package io.bdeploy.minion.plugin;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.bdeploy.bhive.BHive;
import io.bdeploy.bhive.model.Manifest;
import io.bdeploy.bhive.model.Manifest.Key;
import io.bdeploy.bhive.model.ObjectId;
import io.bdeploy.bhive.remote.jersey.BHiveRegistry;
import io.bdeploy.common.util.VersionHelper;
import io.bdeploy.interfaces.manifest.ProductManifest;
import io.bdeploy.interfaces.plugin.PluginHeader;
import io.bdeploy.interfaces.plugin.PluginInfoDto;
import io.bdeploy.interfaces.plugin.PluginManager;
import io.bdeploy.interfaces.plugin.VersionSorterService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * Provides {@link Comparator}s to sort {@link Key}s interpreted as version. Products can contribute specific plugins which allow
 * manipulation of the sorter algorithm. All others can use a default scheme which is "good enough" for most version numbers but
 * might get sorting wrong in some corner cases (e.g. qualifiers with letters, etc.).
 */
public class VersionSorterServiceImpl implements VersionSorterService {

    private static final Logger log = LoggerFactory.getLogger(VersionSorterServiceImpl.class);

    private final PluginManager manager;
    private final BHiveRegistry reg;

    private final Cache<String, Comparator<String>> comparatorCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).build();

    public VersionSorterServiceImpl(PluginManager manager, BHiveRegistry registry) {
        this.manager = manager;
        this.reg = registry;
    }

    private PluginInfoDto getSorterPlugin(String group, Manifest.Key product) {
        BHive hive = reg.get(group);
        if (hive == null) {
            throw new WebApplicationException("Instance Group not found: " + group, Status.NOT_FOUND);
        }

        PluginHeader sorterPlugin = null;
        ObjectId sorterPluginId = null;
        Manifest.Key sorterPluginProduct = null;

        // need to find the product version with the highest version plugin containing a sorter
        SortedSet<Key> scan = ProductManifest.scan(hive);
        for (Manifest.Key scanned : scan) {
            if (!scanned.getName().equals(product.getName())) {
                continue;
            }

            ProductManifest pm = ProductManifest.of(hive, scanned);
            for (ObjectId plugin : pm.getPlugins()) {
                PluginHeader hdr = manager.loadHeader(hive, plugin);
                // ignores plugin name for now.
                if (hdr != null && hdr.sorter
                        && (sorterPlugin == null || VersionHelper.compare(sorterPlugin.version, hdr.version) < 0)) {
                    sorterPlugin = hdr;
                    sorterPluginId = plugin;
                    sorterPluginProduct = scanned;
                    break;
                }
            }
        }

        if (sorterPluginId != null) {
            return manager.load(hive, sorterPluginId, sorterPluginProduct);
        }

        return null;
    }

    /**
     * Creates a comparator which can compare tags for the given product, using a possibly product specific plugin to interpret
     * the tag as version. Versions are sorted ascending (newest version last).
     *
     * @param group the instance group or <code>null</code> to retrieve the default scheme.
     * @param product the product to compare tags for or <code>null</code> to retrieve the default scheme.
     * @return the {@link Comparator}.
     */
    @Override
    public Comparator<String> getTagComparator(String group, Manifest.Key product) {
        if (group == null || product == null) {
            // don't even bother caching this.
            return new DefaultTagAsVersionComparator();
        }

        try {
            return comparatorCache.get(group + "|" + product.getName(), () -> {
                PluginInfoDto dto = getSorterPlugin(group, product);
                if (dto == null) {
                    return new DefaultTagAsVersionComparator();
                }

                if (dto.sorter == null || dto.sorter.getSorter() == null) {
                    log.error("Plugin declared to have a version sorter, but doesn't: {}:{}, origin: {}", dto.name, dto.version,
                            product);
                    return new DefaultTagAsVersionComparator();
                }

                return dto.sorter.getSorter();
            });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Cannot load comparator for " + product + " in " + group, e);
        }
    }

    /**
     * Returns a comparator which sorts by name lexically and by tag descending, using a possibly product specific mechanism.
     *
     * @param group the instance group or <code>null</code> to retrieve the default scheme.
     * @param product the product for which keys are compared. Any key of the product is valid. Pass <code>null</code> to retrieve
     *            the default scheme.
     * @return a {@link Comparator} which can compare keys of the given product.
     */
    @Override
    public Comparator<Manifest.Key> getKeyComparator(String group, Manifest.Key product) {
        Comparator<String> tagComp = getTagComparator(group, product);
        return (a, b) -> {
            int x = a.getName().compareTo(b.getName());
            if (x != 0) {
                return x;
            }
            return tagComp.compare(b.getTag(), a.getTag());
        };
    }

}

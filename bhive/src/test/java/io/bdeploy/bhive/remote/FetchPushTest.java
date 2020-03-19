package io.bdeploy.bhive.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.bdeploy.bhive.BHive;
import io.bdeploy.bhive.TestHive;
import io.bdeploy.bhive.model.Manifest;
import io.bdeploy.bhive.model.Manifest.Key;
import io.bdeploy.bhive.model.ObjectId;
import io.bdeploy.bhive.op.ImportOperation;
import io.bdeploy.bhive.op.ManifestDeleteOperation;
import io.bdeploy.bhive.op.ManifestListOperation;
import io.bdeploy.bhive.op.remote.FetchOperation;
import io.bdeploy.bhive.op.remote.PushOperation;
import io.bdeploy.bhive.op.remote.TransferStatistics;
import io.bdeploy.common.ActivityReporter;
import io.bdeploy.common.ContentHelper;
import io.bdeploy.common.TempDirectory;
import io.bdeploy.common.TempDirectory.TempDir;
import io.bdeploy.common.TestActivityReporter;
import io.bdeploy.common.security.RemoteService;

@ExtendWith(TestHive.class)
@ExtendWith(TempDirectory.class)
@ExtendWith(TestActivityReporter.class)
public class FetchPushTest extends RemoteHiveTestBase {

    @Test
    void rePushWithExistingRemoteRoot(@TempDir Path tmp, RemoteService svc, ActivityReporter r) throws IOException {
        // note: the TestHive.class provided hive is used in the base class, don't use.
        try (BHive local = new BHive(tmp.resolve("h1").toUri(), r); BHive fetchHive = new BHive(tmp.resolve("h2").toUri(), r)) {
            Path src = ContentHelper.genSimpleTestTree(tmp, "app");
            Manifest.Key key = new Manifest.Key("app", "v1");
            local.execute(new ImportOperation().setManifest(key).setSourcePath(src));

            // STEP 1: push the manifest to the remote.
            TransferStatistics s = local.execute(new PushOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(2, s.sumMissingTrees);

            // STEP 2: remove manifest remotely, but don't prune -> root tree still exists in the remote
            getRemote().removeManifest(key);

            // STEP 3: push once more.
            s = local.execute(new PushOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(0, s.sumMissingTrees);

            SortedMap<Key, ObjectId> remoteMfs = getRemote().getManifestInventory();
            assertEquals(1, remoteMfs.size());
            assertEquals(key, remoteMfs.keySet().iterator().next());
        }
    }

    @Test
    void reFetchWithExistingLocalRoot(@TempDir Path tmp, RemoteService svc, ActivityReporter r) throws IOException {
        // note: the TestHive.class provided hive is used in the base class, don't use.
        try (BHive local = new BHive(tmp.resolve("h1").toUri(), r); BHive fetchHive = new BHive(tmp.resolve("h2").toUri(), r)) {
            Path src = ContentHelper.genSimpleTestTree(tmp, "app");
            Manifest.Key key = new Manifest.Key("app", "v1");
            local.execute(new ImportOperation().setManifest(key).setSourcePath(src));

            // STEP 1: push the manifest to the remote.
            local.execute(new PushOperation().setRemote(svc).addManifest(key));

            // STEP 2: fetch from remote to another hive
            TransferStatistics s = fetchHive.execute(new FetchOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(2, s.sumMissingTrees);

            // STEP 3: fetch remote manifest again with existing root tree in local
            fetchHive.execute(new ManifestDeleteOperation().setToDelete(key));
            s = fetchHive.execute(new FetchOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(0, s.sumMissingTrees);

            SortedSet<Key> localMfs = fetchHive.execute(new ManifestListOperation());
            assertEquals(1, localMfs.size());
            assertEquals(key, localMfs.iterator().next());
        }
    }

    @Test
    void pushWithExistingRemoteLeafTree(@TempDir Path tmp, RemoteService svc, ActivityReporter r) throws IOException {
        // note: the TestHive.class provided hive is used in the base class, don't use.
        try (BHive local = new BHive(tmp.resolve("h1").toUri(), r); BHive fetchHive = new BHive(tmp.resolve("h2").toUri(), r)) {
            Path src = ContentHelper.genSimpleTestTree(tmp, "app");
            Manifest.Key key = new Manifest.Key("app", "v1");
            local.execute(new ImportOperation().setManifest(key).setSourcePath(src));

            Manifest.Key subKey = new Manifest.Key("sub", "v1");
            local.execute(new ImportOperation().setManifest(subKey).setSourcePath(src.resolve("dir")));

            // STEP 1: push the manifest to the remote.
            TransferStatistics s = local.execute(new PushOperation().setRemote(svc).addManifest(subKey));
            assertEquals(1, s.sumManifests);
            assertEquals(1, s.sumTrees);
            assertEquals(1, s.sumMissingTrees);

            // STEP 2: remove manifest remotely, but don't prune -> root tree still exists in the remote
            getRemote().removeManifest(subKey);

            // STEP 3: push a manifest which has the previous tree as leaf tree
            s = local.execute(new PushOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(1, s.sumMissingTrees);
        }
    }

    @Test
    void fetchWithExistingRemoteLeafTree(@TempDir Path tmp, RemoteService svc, ActivityReporter r) throws IOException {
        // note: the TestHive.class provided hive is used in the base class, don't use.
        try (BHive local = new BHive(tmp.resolve("h1").toUri(), r); BHive fetchHive = new BHive(tmp.resolve("h2").toUri(), r)) {
            Path src = ContentHelper.genSimpleTestTree(tmp, "app");
            Manifest.Key key = new Manifest.Key("app", "v1");
            local.execute(new ImportOperation().setManifest(key).setSourcePath(src));

            Manifest.Key subKey = new Manifest.Key("sub", "v1");
            local.execute(new ImportOperation().setManifest(subKey).setSourcePath(src.resolve("dir")));

            // STEP 1: push the manifest(s) to the remote.
            local.execute(new PushOperation().setRemote(svc).addManifest(subKey));
            local.execute(new PushOperation().setRemote(svc).addManifest(key));

            // STEP 2: fetch locally
            TransferStatistics s = fetchHive.execute(new FetchOperation().setRemote(svc).addManifest(subKey));
            assertEquals(1, s.sumManifests);
            assertEquals(1, s.sumTrees);
            assertEquals(1, s.sumMissingTrees);

            // STEP 3: remove manifest locally, but don't prune -> root tree still exists in the hive
            fetchHive.execute(new ManifestDeleteOperation().setToDelete(subKey));

            // STEP 4: fetch manifest which has subKeys root as leaf tree
            s = fetchHive.execute(new FetchOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(1, s.sumMissingTrees);
        }
    }

    @Test
    void pushWithPrunedRemoteLeafTree(@TempDir Path tmp, RemoteService svc, ActivityReporter r) throws IOException {
        // note: the TestHive.class provided hive is used in the base class, don't use.
        try (BHive local = new BHive(tmp.resolve("h1").toUri(), r); BHive fetchHive = new BHive(tmp.resolve("h2").toUri(), r)) {
            Path src = ContentHelper.genSimpleTestTree(tmp, "app");
            Manifest.Key key = new Manifest.Key("app", "v1");
            local.execute(new ImportOperation().setManifest(key).setSourcePath(src));

            Manifest.Key subKey = new Manifest.Key("sub", "v1");
            local.execute(new ImportOperation().setManifest(subKey).setSourcePath(src.resolve("dir")));

            // STEP 1: push the manifest to the remote.
            TransferStatistics s = local.execute(new PushOperation().setRemote(svc).addManifest(subKey));
            assertEquals(1, s.sumManifests);
            assertEquals(1, s.sumTrees);
            assertEquals(1, s.sumMissingTrees);

            // STEP 2: remove manifest remotely, but don't prune -> root tree still exists in the remote
            getRemote().removeManifest(subKey);
            getRemote().prune();

            // STEP 3: push a manifest which has the previous tree as leaf tree
            s = local.execute(new PushOperation().setRemote(svc).addManifest(key));
            assertEquals(1, s.sumManifests);
            assertEquals(2, s.sumTrees);
            assertEquals(2, s.sumMissingTrees);
        }
    }

}

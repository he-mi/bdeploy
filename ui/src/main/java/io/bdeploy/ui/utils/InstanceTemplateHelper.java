package io.bdeploy.ui.utils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import io.bdeploy.interfaces.descriptor.template.InstanceTemplateReferenceDescriptor;
import io.bdeploy.ui.dto.ProductDto;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

public class InstanceTemplateHelper {

    private InstanceTemplateHelper() {
    }

    public static ProductDto findMatchingProductOrFail(InstanceTemplateReferenceDescriptor instance, List<ProductDto> products) {
        boolean hasRegex = !(instance.productVersionRegex == null || instance.productVersionRegex.isBlank()
                || instance.productVersionRegex.equals(".*"));

        // the list is ordered - the first matching product is also the best matching version of that product.
        Optional<ProductDto> product = products.stream().filter(p -> {
            if (!p.product.equals(instance.productId)) {
                return false;
            }

            // check whether the version pattern is fulfilled
            if (hasRegex && !Pattern.matches(instance.productVersionRegex, p.key.getTag())) {
                return false;
            }

            // check whether requested template is in this version, otherwise reject.
            return p.instanceTemplates.stream().anyMatch(t -> t.name.equals(instance.templateName));
        }).findFirst();

        if (product.isEmpty()) {
            throw new WebApplicationException(
                    "Cannot find matching product with ID '" + instance.productId
                            + (hasRegex ? ("' (with version matching: " + instance.productVersionRegex + ")") : "'")
                            + " or matching version does not have instance template named '" + instance.templateName + "'",
                    Status.NOT_ACCEPTABLE);
        }

        return product.get();
    }

}

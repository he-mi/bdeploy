package io.bdeploy.common.cfg;

import java.util.regex.Pattern;

import io.bdeploy.common.cfg.Configuration.ConfigValidator;
import io.bdeploy.common.cfg.Configuration.ValidationMessage;

/**
 * Checks hostname value according to https://tools.ietf.org/html/rfc1123 2.1
 *
 * @see #HOSTNAME_PATTERN
 */
@ValidationMessage("Not a valid host name: %s")
public class HostnameValidator implements ConfigValidator<String> {

    private static final Pattern HOSTNAME_PATTERN = Pattern
            .compile("(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)*[a-z0-9][a-z0-9-]{0,61}[a-z0-9]");

    @Override
    public boolean test(String value) {
        return HOSTNAME_PATTERN.matcher(value.toLowerCase()).matches();
    }
}

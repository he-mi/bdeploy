package io.bdeploy.common.cfg;

import java.io.File;
import java.nio.file.Path;

import io.bdeploy.common.cfg.Configuration.ValidationMessage;

/**
 * Checks if the given {@link Path} exists and {@link File#isDirectory() is a directory}.
 */
@ValidationMessage("Directory does not exist, but should exist: %s")
public class ExistingDirectoryValidator extends ExistingPathValidator {

    @Override
    public boolean test(String value) {
        return super.test(value) && p.toFile().isDirectory();
    }
}

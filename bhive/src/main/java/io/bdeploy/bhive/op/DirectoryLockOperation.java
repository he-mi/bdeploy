package io.bdeploy.bhive.op;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bdeploy.common.util.PathHelper;
import io.bdeploy.common.util.StringHelper;
import io.bdeploy.common.util.Threads;

/**
 * Locks a directory. The operations waits for an already existing lock to disappear before proceeding (max wait time is
 * {@value DirectoryModificationOperation#RETRIES} times {@value DirectoryModificationOperation#SLEEP_MILLIS}ms ). This
 * means only a single lock can exist (intra- and inter-VM).
 *
 * @see DirectoryAwaitOperation
 * @see DirectoryReleaseOperation
 */
public class DirectoryLockOperation extends DirectoryModificationOperation {

    private static final Logger log = LoggerFactory.getLogger(DirectoryLockOperation.class);

    @Override
    public void doCall(Path lockFile) {
        String content = "";
        if (getLockContentSupplier() != null) {
            content = getLockContentSupplier().get();
        }

        boolean infoWritten = false;
        for (int i = 0; i < RETRIES; ++i) {
            try {
                Files.write(lockFile, Collections.singletonList(content), StandardOpenOption.CREATE_NEW, StandardOpenOption.SYNC)
                        .toFile().deleteOnExit();
                return;
            } catch (IOException e) {
                // validate to find stale lock files
                if (!isLockFileValid(lockFile, getLockContentValidator())) {
                    continue;
                }
                // inform the user that we're about to wait...
                if (!infoWritten) {
                    log.info("Waiting for {}", directory);
                    infoWritten = true;
                }
                // delay a little...
                if (!Threads.sleep(SLEEP_MILLIS)) {
                    break;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot lock root", e);
            }
        }
        throw new IllegalStateException("Retries exceeded or interrupted while waiting to lock " + lockFile
                + ". Please check manually if another process is still running and delete the lock file manually.");
    }

    /** Validates whether or not the given lock file is still valid */
    static boolean isLockFileValid(Path lockFile, Predicate<String> lockContentValidator) {
        // No content validator. Assuming the lock is still valid
        if (lockContentValidator == null) {
            return true;
        }

        // Read the lock file to check if the content is still valid
        try {
            List<String> lines = Files.readAllLines(lockFile);
            if (!lines.isEmpty() && !StringHelper.isNullOrEmpty(lines.get(0)) && !lockContentValidator.test(lines.get(0))) {
                log.warn("Stale lock file detected, forcefully resolving...");
                PathHelper.deleteIfExistsRetry(lockFile);
                return false;
            }
            return true;
        } catch (NoSuchFileException | FileNotFoundException fne) {
            return false;
        } catch (Exception ve) {
            log.warn("Cannot validate lock file, assuming it is valid: {}: {}", lockFile, ve.toString());
            return true;
        }
    }
}

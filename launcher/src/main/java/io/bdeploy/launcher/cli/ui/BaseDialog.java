package io.bdeploy.launcher.cli.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Base class for all dialogs. Ensures that a common look-and-feel is used.
 */
public class BaseDialog extends JFrame {

    private static final Logger log = LoggerFactory.getLogger(MessageDialog.class);
    private static final long serialVersionUID = 1L;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot set system look&feel", e);
        }
    }

    private final transient Object lock = new Object();

    /**
     * Creates a new centered dialog with the given dimensions
     */
    public BaseDialog(Dimension dimension) {
        setSize(dimension);
        setLayout(new BorderLayout(10, 10));
        setIconImage(WindowHelper.loadImage("/logo128.png"));
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                doClose();
            }

        });
        WindowHelper.center(this);
    }

    /**
     * Blocks the current thread until the main window is closed.
     */
    @SuppressFBWarnings(value = { "UW_UNCOND_WAIT", "WA_NOT_IN_LOOP" })
    public void waitForExit() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Failed to wait until the application is closed", ie);
            }
        }
    }

    /**
     * Invoked when the user closes the dialog.
     */
    protected void doClose() {
        dispose();
        synchronized (lock) {
            lock.notifyAll();
        }
    }

}
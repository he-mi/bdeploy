package io.bdeploy.launcher;

import java.util.HashMap;
import java.util.Map;

import io.bdeploy.interfaces.descriptor.client.ClickAndStartDescriptor;

public class LocalClientApplicationSettings {

    private final Map<String, Boolean> startDescriptor2autostartEnabled = new HashMap<>();
    private final Map<String, ScriptInfo> startScriptName2startDescriptor = new HashMap<>();
    private final Map<String, ScriptInfo> fileAssocScriptName2startDescriptor = new HashMap<>();

    // Autostart

    /**
     * @see Map#put(Object, Object)
     */
    public void putAutostartEnabled(ClickAndStartDescriptor descriptor, boolean enabled) {
        startDescriptor2autostartEnabled.put(stringifyClickAndStartDescriptor(descriptor), enabled);
    }

    /**
     * @see Map#get(Object)
     */
    public Boolean getAutostartEnabled(ClickAndStartDescriptor descriptor) {
        return startDescriptor2autostartEnabled.get(stringifyClickAndStartDescriptor(descriptor));
    }

    private static String stringifyClickAndStartDescriptor(ClickAndStartDescriptor descriptor) {
        return descriptor.groupId + ';' + descriptor.instanceId + ';' + descriptor.applicationId + ';' + descriptor.host.getUri();
    }

    // Scripts

    /**
     * @param scriptName The {@link ScriptInfo} to add to the internal {@link Map}
     * @param scriptInfo The {@link ClickAndStartDescriptor}
     * @param override Will use {@link Map#put(Object, Object) put} if <code>true</code>, otherwise
     *            {@link Map#putIfAbsent(Object, Object) putIfAbsent}
     */
    public void putStartScriptInfo(String scriptName, ScriptInfo scriptInfo, boolean override) {
        if (override) {
            startScriptName2startDescriptor.put(scriptName, scriptInfo);
        } else {
            startScriptName2startDescriptor.putIfAbsent(scriptName, scriptInfo);
        }
    }

    /**
     * @param scriptName The name of the start script
     * @see Map#get(Object)
     */
    public ScriptInfo getStartScriptInfo(String scriptName) {
        return startScriptName2startDescriptor.get(scriptName);
    }

    /**
     * @param scriptName The {@link ScriptInfo} to add to the internal {@link Map}
     * @param scriptInfo The {@link ClickAndStartDescriptor}
     * @param override Will use {@link Map#put(Object, Object) put} if <code>true</code>, otherwise
     *            {@link Map#putIfAbsent(Object, Object) putIfAbsent}
     */
    public void putFileAssocScriptInfo(String scriptName, ScriptInfo scriptInfo, boolean override) {
        if (override) {
            fileAssocScriptName2startDescriptor.put(scriptName, scriptInfo);
        } else {
            fileAssocScriptName2startDescriptor.putIfAbsent(scriptName, scriptInfo);
        }
    }

    /**
     * @param scriptName The name of the file association script
     * @see Map#get(Object)
     */
    public ScriptInfo getFileAssocScriptInfo(String scriptName) {
        return fileAssocScriptName2startDescriptor.get(scriptName);
    }

    public static class ScriptInfo {

        private final String scriptName;
        private final ClickAndStartDescriptor descriptor;

        public ScriptInfo() {
            this(null, null);
        }

        public ScriptInfo(String scriptName, ClickAndStartDescriptor descriptor) {
            this.scriptName = scriptName;
            this.descriptor = descriptor;
        }

        public String getScriptName() {
            return scriptName;
        }

        public ClickAndStartDescriptor getDescriptor() {
            return descriptor;
        }
    }
}

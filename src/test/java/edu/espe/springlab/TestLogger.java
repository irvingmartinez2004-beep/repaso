package edu.espe.springlab;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class TestLogger implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("🟢 TEST PASÓ → " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("🔴 TEST FALLÓ → " + context.getDisplayName());
        System.out.println("    ⚠️ Motivo: " + cause.getMessage());
    }

    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        System.out.println("🟡 TEST DESHABILITADO → " + context.getDisplayName());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("🟠 TEST ABORTADO → " + context.getDisplayName());
    }
}

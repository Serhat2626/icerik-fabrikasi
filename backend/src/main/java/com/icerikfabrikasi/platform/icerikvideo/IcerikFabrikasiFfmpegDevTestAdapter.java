package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-028 Task 02: Dev/test adapter. Calls gate before any result.
 * No ProcessBuilder in this class — boundary contract only.
 */
public class IcerikFabrikasiFfmpegDevTestAdapter {

    private final IcerikFabrikasiFfmpegExecutionGate gate;

    public IcerikFabrikasiFfmpegDevTestAdapter(IcerikFabrikasiFfmpegExecutionGate gate) {
        this.gate = gate != null ? gate : new IcerikFabrikasiFfmpegExecutionGate();
    }

    public IcerikFabrikasiFfmpegDevTestAdapter() {
        this(new IcerikFabrikasiFfmpegExecutionGate());
    }

    /**
     * Execute through gate. Returns safe boundary result + output ref if allowed.
     */
    public IcerikFabrikasiFfmpegDevTestAdapterResult execute(IcerikFabrikasiFfmpegBinaryPolicy binary,
            String jobRef, String queueRef, String reviewRef) {
        var gateResult = gate.evaluate(binary, jobRef, queueRef, reviewRef);

        if (gateResult.getStatus() == IcerikFabrikasiFfmpegBoundaryResultStatus.DENIED)
            return IcerikFabrikasiFfmpegDevTestAdapterResult.denied(gateResult);

        // Gate allowed — produce safe output ref (no real process in this class)
        var outputRef = IcerikFabrikasiFfmpegInternalOutputRef.create();
        return IcerikFabrikasiFfmpegDevTestAdapterResult.completed(gateResult, outputRef);
    }

    public IcerikFabrikasiFfmpegExecutionGate getGate() { return gate; }
}

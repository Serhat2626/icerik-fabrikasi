package com.icerikfabrikasi.platform.icerikvideo;

import java.util.Map;

import static com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiInternalRenderQueueState.*;
import static com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiInternalRenderQueueAction.*;

/**
 * FP-027 Task 02: Queue transition validator. 17-transition matrix. Deny-by-default.
 */
public class IcerikFabrikasiInternalRenderQueueTransitionValidator {

    private record TK(IcerikFabrikasiInternalRenderQueueState from, IcerikFabrikasiInternalRenderQueueAction action) {}
    private record TT(IcerikFabrikasiInternalRenderQueueState to, IcerikFabrikasiInternalRenderQueueReasonCode reason) {}

    private static final Map<TK, TT> ALLOWED = Map.ofEntries(
            // Happy path
            Map.entry(new TK(NOT_QUEUED, MARK_QUEUE_READY),
                    new TT(QUEUE_READY, IcerikFabrikasiInternalRenderQueueReasonCode.QUEUE_READY)),
            Map.entry(new TK(QUEUE_READY, ENQUEUE_INTERNAL),
                    new TT(QUEUED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.ENQUEUED)),
            Map.entry(new TK(QUEUED_INTERNAL, DISPATCH_INTERNAL),
                    new TT(DISPATCHING_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.DISPATCHING)),
            Map.entry(new TK(DISPATCHING_INTERNAL, MARK_IN_PROGRESS),
                    new TT(IN_PROGRESS_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.IN_PROGRESS)),
            Map.entry(new TK(IN_PROGRESS_INTERNAL, MARK_SUCCEEDED),
                    new TT(SUCCEEDED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.SUCCEEDED)),
            Map.entry(new TK(IN_PROGRESS_INTERNAL, MARK_FAILED),
                    new TT(FAILED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.FAILED_PERMANENT)),
            // Retry
            Map.entry(new TK(IN_PROGRESS_INTERNAL, MARK_RETRY_WAIT),
                    new TT(RETRY_WAIT_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.RETRY_WAIT)),
            Map.entry(new TK(RETRY_WAIT_INTERNAL, ENQUEUE_INTERNAL),
                    new TT(QUEUED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.ENQUEUED)),
            // Cancel from multiple states
            Map.entry(new TK(NOT_QUEUED, CANCEL_INTERNAL),
                    new TT(CANCELLED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.CANCELLED)),
            Map.entry(new TK(QUEUE_READY, CANCEL_INTERNAL),
                    new TT(CANCELLED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.CANCELLED)),
            Map.entry(new TK(QUEUED_INTERNAL, CANCEL_INTERNAL),
                    new TT(CANCELLED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.CANCELLED)),
            Map.entry(new TK(DISPATCHING_INTERNAL, CANCEL_INTERNAL),
                    new TT(CANCELLED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.CANCELLED)),
            Map.entry(new TK(IN_PROGRESS_INTERNAL, CANCEL_INTERNAL),
                    new TT(CANCELLED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.CANCELLED)),
            Map.entry(new TK(RETRY_WAIT_INTERNAL, CANCEL_INTERNAL),
                    new TT(CANCELLED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.CANCELLED)),
            // Expire from waiting states
            Map.entry(new TK(QUEUE_READY, EXPIRE_INTERNAL),
                    new TT(EXPIRED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.EXPIRED)),
            Map.entry(new TK(QUEUED_INTERNAL, EXPIRE_INTERNAL),
                    new TT(EXPIRED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.EXPIRED)),
            Map.entry(new TK(RETRY_WAIT_INTERNAL, EXPIRE_INTERNAL),
                    new TT(EXPIRED_INTERNAL, IcerikFabrikasiInternalRenderQueueReasonCode.EXPIRED))
    );

    public IcerikFabrikasiInternalRenderQueueTransitionResult validate(
            IcerikFabrikasiInternalRenderQueueState currentState, IcerikFabrikasiInternalRenderQueueAction action) {
        if (currentState == null)
            return IcerikFabrikasiInternalRenderQueueTransitionResult.denied(null, action,
                    IcerikFabrikasiInternalRenderQueueReasonCode.QUEUE_REF_INVALID);
        if (action == null)
            return IcerikFabrikasiInternalRenderQueueTransitionResult.denied(currentState, null,
                    IcerikFabrikasiInternalRenderQueueReasonCode.TRANSITION_NOT_ALLOWED);
        if (currentState.isTerminal())
            return IcerikFabrikasiInternalRenderQueueTransitionResult.denied(currentState, action,
                    IcerikFabrikasiInternalRenderQueueReasonCode.TERMINAL_STATE);

        var target = ALLOWED.get(new TK(currentState, action));
        if (target != null)
            return IcerikFabrikasiInternalRenderQueueTransitionResult.allowed(currentState, action, target.to(), target.reason());

        return IcerikFabrikasiInternalRenderQueueTransitionResult.denied(currentState, action,
                IcerikFabrikasiInternalRenderQueueReasonCode.TRANSITION_NOT_ALLOWED);
    }

    public int getAllowedTransitionCount() { return ALLOWED.size(); }
}

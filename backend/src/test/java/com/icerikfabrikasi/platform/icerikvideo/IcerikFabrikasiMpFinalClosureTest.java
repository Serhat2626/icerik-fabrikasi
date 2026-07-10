package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MP-003 \u2014 Manual Production Enablement MVP Final Closure")
class IcerikFabrikasiMpFinalClosureTest {

    // All MP + RP classes
    private static final List<Class<?>> ALL_MP_RP_CLASSES = List.of(
            // RP
            IcerikFabrikasiNameStoryboardRequest.class, IcerikFabrikasiNameStoryboardRequestResult.class,
            IcerikFabrikasiNameStoryboardRequestPolicy.class, IcerikFabrikasiNameStoryboardRequestService.class,
            IcerikFabrikasiStoryboardDraftScene.class, IcerikFabrikasiStoryboardDraftResult.class, IcerikFabrikasiStoryboardDraftOrchestrator.class,
            IcerikFabrikasiSceneAssetPlanEntry.class, IcerikFabrikasiSceneAssetPlanResult.class, IcerikFabrikasiSceneAssetPlanner.class,
            IcerikFabrikasiAnimaticTimelineItem.class, IcerikFabrikasiAnimaticTimelinePlanResult.class, IcerikFabrikasiAnimaticTimelinePlanner.class,
            IcerikFabrikasiMvpReadinessResult.class, IcerikFabrikasiMvpReadinessService.class,
            // MP
            IcerikFabrikasiManualEnablementResult.class, IcerikFabrikasiManualEnablementService.class,
            IcerikFabrikasiProductionChecklistItem.class,
            IcerikFabrikasiWorkOrderPreviewResult.class, IcerikFabrikasiWorkOrderPreviewService.class);

    private static final List<Class<? extends Enum<?>>> ALL_ENUMS = List.of(
            IcerikFabrikasiNameStoryboardRequestStatus.class, IcerikFabrikasiNameStoryboardRequestIssueCode.class,
            IcerikFabrikasiStoryboardDraftStatus.class, IcerikFabrikasiStoryboardDraftIssueCode.class,
            IcerikFabrikasiSceneAssetPlanStatus.class, IcerikFabrikasiSceneAssetPlanIssueCode.class,
            IcerikFabrikasiAnimaticTimelinePlanStatus.class, IcerikFabrikasiAnimaticTimelinePlanIssueCode.class,
            IcerikFabrikasiMvpReadinessStatus.class, IcerikFabrikasiMvpReadinessIssueCode.class,
            IcerikFabrikasiManualEnablementStatus.class, IcerikFabrikasiManualEnablementIssueCode.class,
            IcerikFabrikasiManualStepType.class,
            IcerikFabrikasiWorkOrderPreviewStatus.class, IcerikFabrikasiWorkOrderPreviewIssueCode.class);

    private static final List<String> FORBIDDEN_FIELDS = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "publicUrl", "signedUrl", "cdnUrl", "url", "downloadUrl", "publishUrl", "streamUrl",
            "bucketName", "objectKey", "storageKey", "base64", "binaryData", "rawPayload",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "accessKey", "secretKey", "secret",
            "lyrics", "scriptBody", "promptText", "copyrightedContent", "userMedia",
            "unsafeHtml", "html", "audioFile", "videoFile", "mediaFile",
            "modelInput", "modelOutput", "providerPayload", "providerPrompt", "rawPrompt");

    private static final List<String> FORBIDDEN_STATUSES = List.of(
            "COMPLETED", "PUBLISHED", "PUBLICLY_AVAILABLE", "READY_FOR_PUBLISH",
            "READY_FOR_RENDER", "RENDERING", "GENERATED", "DELIVERED", "AUTO_APPROVED");

    // \u2500\u2500 A. Full Chain End-to-End \u2500\u2500
    @Nested @DisplayName("A. Full RP+MP Chain End-to-End")
    class FullChain {
        @Test @DisplayName("T\u00fcrk\u00fc name \u2192 work order ELIGIBLE")
        void endToEnd() {
            var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                    new IcerikFabrikasiNameStoryboardRequest("\u00c7anakkale T\u00fcrk\u00fcs\u00fc", "Marmara", "HALK", "CINEMATIC", 5, 50_000, "MP4", 1920, 1080, 24));
            assertThat(req.getStatus()).isEqualTo(IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY);

            var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
            assertThat(draft.getStatus()).isEqualTo(IcerikFabrikasiStoryboardDraftStatus.READY_FOR_ADMIN_PREVIEW);

            var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
            assertThat(plan.getOverallStatus()).isEqualTo(IcerikFabrikasiSceneAssetPlanStatus.READY_FOR_ASSET_GENERATION_BOUNDARY);

            var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
            assertThat(tl.getStatus()).isEqualTo(IcerikFabrikasiAnimaticTimelinePlanStatus.READY_FOR_ASSEMBLY_BOUNDARY);

            var readiness = new IcerikFabrikasiMvpReadinessService().assess(req, draft, plan, tl);
            assertThat(readiness.getStatus()).isEqualTo(IcerikFabrikasiMvpReadinessStatus.READY_FOR_MANUAL_REVIEW);

            var enablement = new IcerikFabrikasiManualEnablementService().evaluate(readiness);
            assertThat(enablement.getStatus()).isEqualTo(IcerikFabrikasiManualEnablementStatus.ELIGIBLE_FOR_MANUAL_PREP);

            var workOrder = new IcerikFabrikasiWorkOrderPreviewService().generate(enablement);
            assertThat(workOrder.getStatus()).isEqualTo(IcerikFabrikasiWorkOrderPreviewStatus.ELIGIBLE_FOR_MANUAL_PREPARATION);
            assertThat(workOrder.getChecklistItems()).hasSizeGreaterThanOrEqualTo(6);
            assertThat(workOrder.getCompletedItems()).isEqualTo(workOrder.getTotalItems());
        }

        @Test @DisplayName("all orchestrators deterministic fake")
        void deterministicMode() {
            var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                    new IcerikFabrikasiNameStoryboardRequest("Test", null, null, null, 3, 30_000, "MP4", 1920, 1080, 24));
            var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
            assertThat(draft.getOrchestrationMode()).isEqualTo("DETERMINISTIC_FAKE_TEST_DOUBLE");
            var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
            assertThat(plan.getPlannerMode()).isEqualTo("DETERMINISTIC_FAKE_TEST_DOUBLE");
            var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
            assertThat(tl.getPlannerMode()).isEqualTo("DETERMINISTIC_FAKE_TEST_DOUBLE");
        }

        @Test @DisplayName("checklist labels only, work order non-executable")
        void labelsOnly() {
            var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                    new IcerikFabrikasiNameStoryboardRequest("Test", null, null, null, 3, 30_000, "MP4", 1920, 1080, 24));
            var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
            var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
            var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
            var r = new IcerikFabrikasiMvpReadinessService().assess(req, draft, plan, tl);
            var e = new IcerikFabrikasiManualEnablementService().evaluate(r);
            var wo = new IcerikFabrikasiWorkOrderPreviewService().generate(e);
            for (var item : wo.getChecklistItems()) {
                assertThat(item.getLabel()).isInstanceOf(String.class).isNotBlank();
                assertThat(item.getInstruction()).isInstanceOf(String.class).isNotBlank();
            }
            for (var step : wo.getWorkOrderStepLabels())
                assertThat(step).isInstanceOf(String.class).isNotBlank();
        }
    }

    // \u2500\u2500 B. Forbidden Field Sweep (20 classes) \u2500\u2500
    @Nested @DisplayName("B. Forbidden Field Sweep (20 RP+MP classes)")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields across all classes")
        void sweep() {
            for (var c : ALL_MP_RP_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN_FIELDS)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // \u2500\u2500 C. No Forbidden Statuses (15 enums) \u2500\u2500
    @Nested @DisplayName("C. No Forbidden Statuses (15 enums)")
    class NoForbiddenStatuses {
        @Test @DisplayName("no COMPLETED/PUBLISHED/RENDER/GENERATED/DELIVERED as exact status")
        void noForbidden() {
            for (var enumClass : ALL_ENUMS) {
                for (var v : enumClass.getEnumConstants()) {
                    for (var forbidden : FORBIDDEN_STATUSES)
                        assertThat(v.name()).as("%s.%s", enumClass.getSimpleName(), v.name())
                                .isNotEqualTo(forbidden);
                }
            }
        }
    }

    // \u2500\u2500 D. Endpoint Unchanged \u2500\u2500
    @Nested @DisplayName("D. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class Endpoints {
        @Test @DisplayName("total unchanged")
        void total() {
            long post = count(AdminReviewController.class, PostMapping.class)
                    + count(AdminPipelineController.class, PostMapping.class)
                    + count(AdminWorkflowObservabilityController.class, PostMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = count(AdminReviewController.class, GetMapping.class)
                    + count(AdminPipelineController.class, GetMapping.class)
                    + count(AdminWorkflowObservabilityController.class, GetMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post + get).as("total").isEqualTo(46);
            assertThat(post).as("POST").isEqualTo(26);
            assertThat(get).as("GET").isEqualTo(20);
        }
        private long count(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
            return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).count();
        }
    }

    // \u2500\u2500 E. ProcessBuilder / Runtime \u2500\u2500
    @Nested @DisplayName("E. ProcessBuilder / Runtime (20 classes)")
    class ProcessCheck {
        @Test @DisplayName("no ProcessBuilder/Runtime in RP+MP classes")
        void clean() {
            for (var c : ALL_MP_RP_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).as(c.getSimpleName())
                        .doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }

    // \u2500\u2500 F. Chain Rejection \u2500\u2500
    @Nested @DisplayName("F. Chain Rejection Propagation")
    class Rejection {
        @Test @DisplayName("null readiness -> enablement NOT_ELIGIBLE -> work order BLOCKED")
        void fullReject() {
            var e = new IcerikFabrikasiManualEnablementService().evaluate(null);
            assertThat(e.getStatus()).isEqualTo(IcerikFabrikasiManualEnablementStatus.NOT_ELIGIBLE);
            var wo = new IcerikFabrikasiWorkOrderPreviewService().generate(e);
            assertThat(wo.getStatus()).isEqualTo(IcerikFabrikasiWorkOrderPreviewStatus.BLOCKED_BY_SAFETY);
        }
    }

    // \u2500\u2500 G. Copyright Safety \u2500\u2500
    @Nested @DisplayName("G. Copyright Safety")
    class Copyright {
        @Test @DisplayName("chain preserves copyright-safe intent")
        void intent() {
            var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                    new IcerikFabrikasiNameStoryboardRequest("Test", null, null, null, 3, 30_000, "MP4", 1920, 1080, 24));
            assertThat(req.getCopyrightSafeIntentSummary()).contains("telif");
            var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
            var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
            var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
            var r = new IcerikFabrikasiMvpReadinessService().assess(req, draft, plan, tl);
            assertThat(r.isCopyrightSafeIntentPresent()).isTrue();
            var e = new IcerikFabrikasiManualEnablementService().evaluate(r);
            assertThat(e.isCopyrightSafeIntentPresent()).isTrue();
        }
    }

    // \u2500\u2500 H. Enum Inventory \u2500\u2500
    @Nested @DisplayName("H. Enum Inventory Snapshot")
    class EnumInventory {
        @Test @DisplayName("MP enum sizes")
        void mpSizes() {
            assertThat(IcerikFabrikasiManualEnablementStatus.values()).hasSize(5);
            assertThat(IcerikFabrikasiManualEnablementIssueCode.values()).hasSize(8);
            assertThat(IcerikFabrikasiManualStepType.values()).hasSize(6);
            assertThat(IcerikFabrikasiWorkOrderPreviewStatus.values()).hasSize(5);
            assertThat(IcerikFabrikasiWorkOrderPreviewIssueCode.values()).hasSize(6);
        }
        @Test @DisplayName("RP enum sizes")
        void rpSizes() {
            assertThat(IcerikFabrikasiNameStoryboardRequestStatus.values()).hasSize(6);
            assertThat(IcerikFabrikasiStoryboardDraftStatus.values()).hasSize(7);
            assertThat(IcerikFabrikasiSceneAssetPlanStatus.values()).hasSize(7);
            assertThat(IcerikFabrikasiAnimaticTimelinePlanStatus.values()).hasSize(6);
            assertThat(IcerikFabrikasiMvpReadinessStatus.values()).hasSize(5);
        }
    }
}

package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LIVE-005 — Human Review + Controlled Delivery + Final Closure")
class IcerikFabrikasiLiveReviewDeliveryTest {

    private static final List<String> FORBIDDEN = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "publicUrl", "signedUrl", "cdnUrl", "url", "downloadUrl", "publishUrl", "streamUrl",
            "bucketName", "objectKey", "storageKey", "base64", "binaryData", "rawPayload",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "accessKey", "secretKey", "secret", "apiKey", "credential", "password",
            "lyrics", "scriptBody", "copyrightedContent",
            "unsafeHtml", "html", "providerPayload", "providerPrompt", "rawPrompt",
            "command", "args", "processBuilder");

    private static final List<Class<?>> LIVE005_CLASSES = List.of(
            IcerikFabrikasiLiveReviewChecklistItem.class, IcerikFabrikasiLiveControlledDeliveryResult.class,
            IcerikFabrikasiLiveReviewDeliveryService.class);

    private static final List<String> FORBIDDEN_STATUSES = List.of(
            "COMPLETED", "PUBLISHED", "PUBLICLY_AVAILABLE", "READY_FOR_PUBLISH",
            "RENDERED", "GENERATED", "DELIVERED", "AUTO_APPROVED", "AUTO_PUBLISHED");

    private IcerikFabrikasiLiveReviewDeliveryService service;

    @BeforeEach void setUp() { service = new IcerikFabrikasiLiveReviewDeliveryService(); }

    private IcerikFabrikasiLiveRenderResult fakeRender() {
        var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                new IcerikFabrikasiNameStoryboardRequest("Test", "Test", "HALK", "CINEMATIC", 4, 40_000, "MP4", 1920, 1080, 24));
        var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
        var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
        var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
        var r = new IcerikFabrikasiMvpReadinessService().assess(req, draft, plan, tl);
        var e = new IcerikFabrikasiManualEnablementService().evaluate(r);
        var wo = new IcerikFabrikasiWorkOrderPreviewService().generate(e);
        var gate = new IcerikFabrikasiExecutionGateService().evaluate(wo);
        var prov = new IcerikFabrikasiProviderConfigService().evaluate(gate);
        var render = new IcerikFabrikasiRenderPipelineConfigService().evaluate(prov);
        var exec = new IcerikFabrikasiExecutionPackageService().assemble(wo, prov, render);
        var prompt = new IcerikFabrikasiImagePromptPackageService().prepare(exec);
        var gen = new IcerikFabrikasiLiveImageGenerationService().executeFake(prompt);
        var storage = new IcerikFabrikasiLiveArtifactStorageService().storeFake(gen);
        return new IcerikFabrikasiLiveRenderExecutionService().renderFake(storage);
    }

    // A. Pending Review Blocks Delivery
    @Nested @DisplayName("A. Pending Review Blocks Delivery")
    class PendingBlocks {
        @Test @DisplayName("null review -> PENDING_HUMAN_REVIEW")
        void nullReview() {
            var r = service.evaluate(fakeRender(), null, null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveDeliveryStatus.PENDING_HUMAN_REVIEW);
            assertThat(r.isExportEligible()).isFalse();
        }
        @Test @DisplayName("PENDING -> blocked")
        void pendingReview() {
            var r = service.evaluate(fakeRender(), IcerikFabrikasiLiveReviewDecision.PENDING, null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveDeliveryStatus.PENDING_HUMAN_REVIEW);
            assertThat(r.isExportEligible()).isFalse();
        }
    }

    // B. Rejected Blocks Delivery
    @Nested @DisplayName("B. Rejected Blocks Delivery")
    class RejectedBlocks {
        @Test @DisplayName("REJECTED -> BLOCKED_BY_REVIEW")
        void rejected() {
            var r = service.evaluate(fakeRender(), IcerikFabrikasiLiveReviewDecision.REJECTED, null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveDeliveryStatus.BLOCKED_BY_REVIEW);
            assertThat(r.isExportEligible()).isFalse();
            assertThat(r.getIssues()).contains(IcerikFabrikasiLiveDeliveryIssueCode.REVIEW_REJECTED);
        }
    }

    // C. Needs Revision Blocks
    @Nested @DisplayName("C. Needs Revision Blocks")
    class RevisionBlocks {
        @Test @DisplayName("NEEDS_REVISION -> BLOCKED_BY_REVIEW")
        void revision() {
            var r = service.evaluate(fakeRender(), IcerikFabrikasiLiveReviewDecision.NEEDS_REVISION, null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveDeliveryStatus.BLOCKED_BY_REVIEW);
            assertThat(r.isExportEligible()).isFalse();
        }
    }

    // D. Approved -> Controlled Delivery
    @Nested @DisplayName("D. Approved -> Controlled Delivery")
    class Approved {
        @Test @DisplayName("ELIGIBLE_FOR_CONTROLLED_DELIVERY")
        void eligible() {
            var r = service.evaluate(fakeRender(), IcerikFabrikasiLiveReviewDecision.APPROVED_FOR_CONTROLLED_DELIVERY, null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveDeliveryStatus.ELIGIBLE_FOR_CONTROLLED_DELIVERY);
            assertThat(r.isExportEligible()).isTrue();
            assertThat(r.getOpaqueDeliveryRef()).startsWith("ldlv:");
            assertThat(r.getReviewDecision()).isEqualTo(IcerikFabrikasiLiveReviewDecision.APPROVED_FOR_CONTROLLED_DELIVERY);
        }
        @Test @DisplayName("export note mentions admin only")
        void adminOnly() {
            var r = service.evaluate(fakeRender(), IcerikFabrikasiLiveReviewDecision.APPROVED_FOR_CONTROLLED_DELIVERY, null);
            assertThat(r.getExportEligibilityNote()).contains("admin");
        }
    }

    // E. No Render -> NOT_ELIGIBLE
    @Nested @DisplayName("E. No Render -> NOT_ELIGIBLE")
    class NoRender {
        @Test @DisplayName("null render -> NOT_ELIGIBLE")
        void nullRender() {
            var r = service.evaluate(null, IcerikFabrikasiLiveReviewDecision.APPROVED_FOR_CONTROLLED_DELIVERY, null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveDeliveryStatus.NOT_ELIGIBLE);
        }
    }

    // F. Review Checklist
    @Nested @DisplayName("F. Review Checklist")
    class Checklist {
        @Test @DisplayName("default checklist has 6 items")
        void defaultItems() {
            assertThat(IcerikFabrikasiLiveReviewChecklistItem.defaultChecklist()).hasSize(6);
        }
        @Test @DisplayName("all default items unchecked")
        void unchecked() {
            for (var item : IcerikFabrikasiLiveReviewChecklistItem.defaultChecklist())
                assertThat(item.isChecked()).isFalse();
        }
    }

    // G. Forbidden Sweep
    @Nested @DisplayName("G. Forbidden Field Sweep")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields")
        void sweep() {
            for (var c : LIVE005_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // H. No Forbidden Statuses
    @Nested @DisplayName("H. No Forbidden Statuses")
    class NoForbidden {
        @Test @DisplayName("delivery status safe")
        void safe() {
            for (var v : IcerikFabrikasiLiveDeliveryStatus.values())
                for (var f : FORBIDDEN_STATUSES)
                    assertThat(v.name()).isNotEqualTo(f);
        }
        @Test @DisplayName("review decision safe")
        void reviewSafe() {
            for (var v : IcerikFabrikasiLiveReviewDecision.values())
                for (var f : FORBIDDEN_STATUSES)
                    assertThat(v.name()).isNotEqualTo(f);
        }
        @Test @DisplayName("6 delivery statuses")
        void count() { assertThat(IcerikFabrikasiLiveDeliveryStatus.values()).hasSize(6); }
        @Test @DisplayName("4 review decisions")
        void decisions() { assertThat(IcerikFabrikasiLiveReviewDecision.values()).hasSize(4); }
    }

    // I. Endpoint Unchanged
    @Nested @DisplayName("I. Endpoint Inventory (46 = 26 POST + 20 GET)")
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
            assertThat(post + get).isEqualTo(46);
        }
        private long count(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
            return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).count();
        }
    }

    // J. ProcessBuilder / Runtime
    @Nested @DisplayName("J. ProcessBuilder / Runtime")
    class ProcessCheck {
        @Test @DisplayName("no ProcessBuilder/Runtime")
        void clean() {
            for (var c : LIVE005_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }
}

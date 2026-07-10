package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RPE-005 — Safe Image Prompt Package + Manual Approval Gate")
class IcerikFabrikasiImagePromptPackageTest {

    private static final List<String> FORBIDDEN = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "publicUrl", "signedUrl", "cdnUrl", "url", "downloadUrl", "publishUrl", "streamUrl",
            "bucketName", "objectKey", "storageKey", "base64", "binaryData", "rawPayload",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "accessKey", "secretKey", "secret", "apiKey", "credential", "password",
            "lyrics", "scriptBody", "copyrightedContent",
            "unsafeHtml", "html", "providerPayload", "providerPrompt", "rawPrompt");

    private static final List<Class<?>> RPE005_CLASSES = List.of(
            IcerikFabrikasiSceneImagePrompt.class, IcerikFabrikasiImagePromptPackageResult.class,
            IcerikFabrikasiImagePromptPackageService.class);

    private static final List<String> FORBIDDEN_STATUSES = List.of(
            "COMPLETED", "PUBLISHED", "PUBLICLY_AVAILABLE", "READY_FOR_PUBLISH",
            "READY_FOR_RENDER", "RENDERING", "GENERATED", "DELIVERED", "AUTO_APPROVED",
            "PROVIDER_ENABLED", "IMAGE_GENERATION_READY");

    private IcerikFabrikasiImagePromptPackageService service;

    @BeforeEach void setUp() { service = new IcerikFabrikasiImagePromptPackageService(); }

    private IcerikFabrikasiExecutionPackageResult execPackage() {
        var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                new IcerikFabrikasiNameStoryboardRequest("Canakkale", "Marmara", "HALK", "CINEMATIC", 4, 40_000, "MP4", 1920, 1080, 24));
        var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
        var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
        var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
        var r = new IcerikFabrikasiMvpReadinessService().assess(req, draft, plan, tl);
        var e = new IcerikFabrikasiManualEnablementService().evaluate(r);
        var wo = new IcerikFabrikasiWorkOrderPreviewService().generate(e);
        var gate = new IcerikFabrikasiExecutionGateService().evaluate(wo);
        var prov = new IcerikFabrikasiProviderConfigService().evaluate(gate);
        var render = new IcerikFabrikasiRenderPipelineConfigService().evaluate(prov);
        return new IcerikFabrikasiExecutionPackageService().assemble(wo, prov, render);
    }

    // A. Prompt Package Created
    @Nested @DisplayName("A. Prompt Package Created")
    class Created {
        @Test @DisplayName("PENDING_MANUAL_APPROVAL")
        void pending() {
            var r = service.prepare(execPackage());
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiImagePromptPackageStatus.PENDING_MANUAL_APPROVAL);
            assertThat(r.getOpaquePromptPackageRef()).startsWith("ipkg:");
            assertThat(r.getSafeIcerikFabrikasiTitleLabel()).isEqualTo("Canakkale");
        }
        @Test @DisplayName("4 scene prompts")
        void scenes() {
            var r = service.prepare(execPackage());
            assertThat(r.getScenePrompts()).hasSize(4);
            assertThat(r.getTotalScenes()).isEqualTo(4);
            assertThat(r.getBlockedScenes()).isEqualTo(0);
        }
        @Test @DisplayName("manual approval not granted")
        void notApproved() {
            var r = service.prepare(execPackage());
            assertThat(r.isManualApprovalGranted()).isFalse();
        }
    }

    // B. Scene Prompt Content
    @Nested @DisplayName("B. Scene Prompt Content")
    class Content {
        @Test @DisplayName("safe prompt summary present")
        void summary() {
            var r = service.prepare(execPackage());
            for (var p : r.getScenePrompts()) {
                assertThat(p.getSafePromptSummary()).isNotBlank();
                assertThat(p.getSafeNegativeConstraints()).isNotBlank();
                assertThat(p.getAspectRatio()).isEqualTo("16:9");
                assertThat(p.isBlocked()).isFalse();
            }
        }
        @Test @DisplayName("safety constraints per scene")
        void constraints() {
            var r = service.prepare(execPackage());
            for (var p : r.getScenePrompts())
                assertThat(p.getSafetyConstraintLabels()).hasSizeGreaterThanOrEqualTo(5);
        }
    }

    // C. Blocked
    @Nested @DisplayName("C. Blocked / Missing")
    class Blocked {
        @Test @DisplayName("null exec -> BLOCKED_BY_SAFETY")
        void nullExec() {
            var r = service.prepare(null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiImagePromptPackageStatus.BLOCKED_BY_SAFETY);
            assertThat(r.getIssues()).contains(IcerikFabrikasiImagePromptPackageIssueCode.EXECUTION_PACKAGE_NOT_READY);
            assertThat(r.getScenePrompts()).isEmpty();
        }
    }

    // D. Forbidden Sweep
    @Nested @DisplayName("D. Forbidden Field Sweep")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields")
        void sweep() {
            for (var c : RPE005_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // E. No Forbidden Statuses
    @Nested @DisplayName("E. No Forbidden Statuses")
    class NoForbidden {
        @Test @DisplayName("status safe")
        void safe() {
            for (var v : IcerikFabrikasiImagePromptPackageStatus.values())
                for (var f : FORBIDDEN_STATUSES)
                    assertThat(v.name()).isNotEqualTo(f);
        }
        @Test @DisplayName("exactly 5 statuses")
        void count() { assertThat(IcerikFabrikasiImagePromptPackageStatus.values()).hasSize(5); }
        @Test @DisplayName("exactly 6 issue codes")
        void issues() { assertThat(IcerikFabrikasiImagePromptPackageIssueCode.values()).hasSize(6); }
    }

    // F. Endpoint Unchanged
    @Nested @DisplayName("F. Endpoint Inventory (46 = 26 POST + 20 GET)")
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

    // G. ProcessBuilder / Runtime
    @Nested @DisplayName("G. ProcessBuilder / Runtime")
    class ProcessCheck {
        @Test @DisplayName("no ProcessBuilder/Runtime")
        void clean() {
            for (var c : RPE005_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }

    // H. Prompt Text Safety
    @Nested @DisplayName("H. Prompt Text Safety")
    class PromptSafety {
        @Test @DisplayName("prompts contain no forbidden text")
        void noForbidden() {
            var r = service.prepare(execPackage());
            for (var p : r.getScenePrompts())
                for (var f : FORBIDDEN)
                    assertThat(p.getSafePromptSummary()).doesNotContain(f);
        }
    }
}

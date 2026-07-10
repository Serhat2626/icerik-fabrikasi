package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RPE-008 — Real Production Execution MVP Final Closure")
class IcerikFabrikasiRpePhaseClosureTest {

    private static final List<String> FORBIDDEN_FIELDS = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "publicUrl", "signedUrl", "cdnUrl", "url", "downloadUrl", "publishUrl", "streamUrl",
            "bucketName", "objectKey", "storageKey", "base64", "binaryData", "rawPayload",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "accessKey", "secretKey", "secret", "apiKey", "credential", "password",
            "lyrics", "scriptBody", "copyrightedContent",
            "unsafeHtml", "html", "providerPayload", "providerPrompt", "rawPrompt");

    private static final List<String> FORBIDDEN_STATUSES = List.of(
            "COMPLETED", "PUBLISHED", "PUBLICLY_AVAILABLE", "READY_FOR_PUBLISH",
            "READY_FOR_RENDER", "RENDERING", "GENERATED", "DELIVERED", "AUTO_APPROVED",
            "PROVIDER_ENABLED", "IMAGE_GENERATION_READY");

    private static final List<Class<?>> ALL_RPE_CLASSES = List.of(
            // RPE-001
            IcerikFabrikasiExecutionGateResult.class, IcerikFabrikasiExecutionGateService.class,
            // RPE-002
            IcerikFabrikasiProviderConfigResult.class, IcerikFabrikasiProviderConfigService.class,
            // RPE-003
            IcerikFabrikasiRenderEnvironmentBoundary.class, IcerikFabrikasiFfmpegCapabilityMetadata.class,
            IcerikFabrikasiOutputFormatPolicy.class, IcerikFabrikasiRenderPipelineConfigResult.class, IcerikFabrikasiRenderPipelineConfigService.class,
            // RPE-004
            IcerikFabrikasiManualGate.class, IcerikFabrikasiExecutionPackageResult.class, IcerikFabrikasiExecutionPackageService.class,
            // RPE-005
            IcerikFabrikasiSceneImagePrompt.class, IcerikFabrikasiImagePromptPackageResult.class, IcerikFabrikasiImagePromptPackageService.class,
            // RPE-006
            IcerikFabrikasiFakeImageResultMetadata.class, IcerikFabrikasiImageGenerationExecutionResult.class, IcerikFabrikasiImageGenerationExecutionService.class,
            // RPE-007
            IcerikFabrikasiSceneArtifactReadiness.class, IcerikFabrikasiArtifactVerificationReport.class,
            IcerikFabrikasiRenderPackageResult.class, IcerikFabrikasiArtifactVerificationService.class);

    @SuppressWarnings("rawtypes")
    private static final List<Class<? extends Enum>> ALL_RPE_ENUMS = List.of(
            IcerikFabrikasiExecutionGateStatus.class, IcerikFabrikasiExecutionGateIssueCode.class,
            IcerikFabrikasiProviderConfigStatus.class, IcerikFabrikasiProviderConfigIssueCode.class,
            IcerikFabrikasiRenderConfigStatus.class, IcerikFabrikasiRenderConfigIssueCode.class,
            IcerikFabrikasiExecutionPackageStatus.class, IcerikFabrikasiExecutionPackageIssueCode.class,
            IcerikFabrikasiImagePromptPackageStatus.class, IcerikFabrikasiImagePromptPackageIssueCode.class,
            IcerikFabrikasiImageGenerationExecutionMode.class, IcerikFabrikasiImageGenerationStatus.class, IcerikFabrikasiImageGenerationIssueCode.class,
            IcerikFabrikasiArtifactVerificationStatus.class, IcerikFabrikasiRenderPackageStatus.class);

    // ── Full E2E chain helper ──
    private IcerikFabrikasiRenderPackageResult fullChain() {
        var req = new IcerikFabrikasiNameStoryboardRequestService().createRequest(
                new IcerikFabrikasiNameStoryboardRequest("Canakkale", "Marmara", "HALK", "CINEMATIC", 4, 40_000, "MP4", 1920, 1080, 24));
        var draft = new IcerikFabrikasiStoryboardDraftOrchestrator().orchestrate(req);
        var plan = new IcerikFabrikasiSceneAssetPlanner().plan(draft);
        var tl = new IcerikFabrikasiAnimaticTimelinePlanner().plan(plan);
        var r = new IcerikFabrikasiMvpReadinessService().assess(req, draft, plan, tl);
        var e = new IcerikFabrikasiManualEnablementService().evaluate(r);
        var wo = new IcerikFabrikasiWorkOrderPreviewService().generate(e);
        // RPE-001
        var gate = new IcerikFabrikasiExecutionGateService().evaluate(wo);
        // RPE-002
        var prov = new IcerikFabrikasiProviderConfigService().evaluate(gate);
        // RPE-003
        var render = new IcerikFabrikasiRenderPipelineConfigService().evaluate(prov);
        // RPE-004
        var exec = new IcerikFabrikasiExecutionPackageService().assemble(wo, prov, render);
        // RPE-005
        var prompt = new IcerikFabrikasiImagePromptPackageService().prepare(exec);
        // RPE-006
        var gen = new IcerikFabrikasiImageGenerationExecutionService().execute(prompt);
        // RPE-007
        var verf = new IcerikFabrikasiArtifactVerificationService();
        var report = verf.verify(gen);
        return verf.prepareRenderPackage(report);
    }

    // A. Full E2E Chain
    @Nested @DisplayName("A. Full RPE E2E Chain")
    class E2EChain {
        @Test @DisplayName("chain produces render package with WAITING_FOR_MANUAL_GATE")
        void chain() {
            var r = fullChain();
            assertThat(r).isNotNull();
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiRenderPackageStatus.WAITING_FOR_MANUAL_GATE);
            assertThat(r.getOpaqueRenderPackageRef()).startsWith("rpkg:");
        }
        @Test @DisplayName("local/manual render disabled")
        void renderDisabled() {
            assertThat(fullChain().isLocalManualRenderEnabled()).isFalse();
        }
        @Test @DisplayName("render prerequisites present")
        void prereqs() {
            assertThat(fullChain().getRenderPrerequisiteLabels()).hasSizeGreaterThanOrEqualTo(5);
        }
    }

    // B. Forbidden Field Sweep — All RPE Classes
    @Nested @DisplayName("B. Forbidden Field Sweep (all RPE classes)")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields across 22 RPE classes")
        void sweep() {
            for (var c : ALL_RPE_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN_FIELDS)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // C. Forbidden Status Sweep — All RPE Enums
    @Nested @DisplayName("C. Forbidden Status Sweep (all RPE enums)")
    class ForbiddenStatusSweep {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Test @DisplayName("0 forbidden statuses across 15 RPE enums")
        void sweep() {
            for (Class<? extends Enum> ec : ALL_RPE_ENUMS) {
                for (Enum<?> v : ec.getEnumConstants())
                    for (String f : FORBIDDEN_STATUSES)
                        assertThat(v.name()).as("%s.%s", ec.getSimpleName(), v.name()).isNotEqualTo(f);
            }
        }
    }

    // D. ProcessBuilder / Runtime Sweep
    @Nested @DisplayName("D. ProcessBuilder / Runtime Sweep")
    class ProcessSweep {
        @Test @DisplayName("no ProcessBuilder/Runtime in RPE classes")
        void clean() {
            for (var c : ALL_RPE_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).as(c.getSimpleName()).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }

    // E. Endpoint Inventory Unchanged
    @Nested @DisplayName("E. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class Endpoints {
        @Test @DisplayName("POST count = 26")
        void post() {
            long post = count(AdminReviewController.class, PostMapping.class)
                    + count(AdminPipelineController.class, PostMapping.class)
                    + count(AdminWorkflowObservabilityController.class, PostMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            assertThat(post).isEqualTo(26);
        }
        @Test @DisplayName("GET count = 20")
        void get() {
            long get = count(AdminReviewController.class, GetMapping.class)
                    + count(AdminPipelineController.class, GetMapping.class)
                    + count(AdminWorkflowObservabilityController.class, GetMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(get).isEqualTo(20);
        }
        @Test @DisplayName("total = 46")
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

    // F. Execution Mode Default
    @Nested @DisplayName("F. Execution Mode Defaults")
    class ModeDefaults {
        @Test @DisplayName("CI_FAKE is default generation mode")
        void genMode() {
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
            var gen = new IcerikFabrikasiImageGenerationExecutionService().execute(prompt);
            assertThat(gen.getExecutionMode()).isEqualTo(IcerikFabrikasiImageGenerationExecutionMode.CI_FAKE);
            assertThat(gen.isManualGatePassed()).isFalse();
        }
    }

    // G. Manual Gates Never Auto-Approved
    @Nested @DisplayName("G. Manual Gates Never Auto-Approved")
    class ManualGates {
        @Test @DisplayName("execution package gates all unpassed")
        void execGates() {
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
            for (var g : exec.getManualGates()) assertThat(g.isPassed()).isFalse();
        }
        @Test @DisplayName("prompt package approval not granted")
        void promptApproval() {
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
            assertThat(prompt.isManualApprovalGranted()).isFalse();
        }
    }
}

package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LIVE-004 — Manual-Gated Real FFmpeg Render + Render Artifact Verification")
class IcerikFabrikasiLiveRenderExecutionTest {

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

    private static final List<Class<?>> LIVE004_CLASSES = List.of(
            IcerikFabrikasiLiveRenderArtifact.class, IcerikFabrikasiLiveRenderResult.class,
            IcerikFabrikasiLiveRenderExecutionService.class);

    private static final List<String> FORBIDDEN_STATUSES = List.of(
            "COMPLETED", "PUBLISHED", "PUBLICLY_AVAILABLE", "READY_FOR_PUBLISH",
            "RENDERING", "GENERATED", "DELIVERED", "AUTO_APPROVED");

    private IcerikFabrikasiLiveRenderExecutionService service;

    @BeforeEach void setUp() { service = new IcerikFabrikasiLiveRenderExecutionService(); }

    private IcerikFabrikasiLiveStorageResult fakeStorage() {
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
        var exec = new IcerikFabrikasiExecutionPackageService().assemble(wo, prov, render);
        var prompt = new IcerikFabrikasiImagePromptPackageService().prepare(exec);
        var gen = new IcerikFabrikasiLiveImageGenerationService().executeFake(prompt);
        return new IcerikFabrikasiLiveArtifactStorageService().storeFake(gen);
    }

    // A. CI Fake Render
    @Nested @DisplayName("A. CI Fake Render")
    class FakeRender {
        @Test @DisplayName("FAKE_RENDER_AVAILABLE")
        void fake() {
            var r = service.renderFake(fakeStorage());
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveRenderStatus.FAKE_RENDER_AVAILABLE);
            assertThat(r.getMode()).isEqualTo(IcerikFabrikasiLiveRenderMode.CI_FAKE);
            assertThat(r.getRenderArtifact()).isNotNull();
            assertThat(r.getRenderArtifact().isVerified()).isTrue();
        }
        @Test @DisplayName("null storage -> FAILED_SAFE")
        void nullStorage() {
            var r = service.renderFake(null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveRenderStatus.FAILED_SAFE);
        }
    }

    // B. Kill Switch Blocks
    @Nested @DisplayName("B. Kill Switch Blocks Real Render")
    class KillBlock {
        @Test @DisplayName("kill switch -> BLOCKED_BY_KILL_SWITCH")
        void blocked() {
            var r = service.renderReal(fakeStorage(), IcerikFabrikasiKillSwitch.defaultActive(), true);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveRenderStatus.BLOCKED_BY_KILL_SWITCH);
            assertThat(r.getRenderArtifact()).isNull();
        }
        @Test @DisplayName("null kill switch -> default active")
        void nullKill() {
            var r = service.renderReal(fakeStorage(), null, true);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveRenderStatus.BLOCKED_BY_KILL_SWITCH);
        }
    }

    // C. Operator Gate Blocks
    @Nested @DisplayName("C. Operator Gate Blocks")
    class OpBlock {
        @Test @DisplayName("gate not passed -> BLOCKED_BY_OPERATOR_GATE")
        void blocked() {
            var r = service.renderReal(fakeStorage(), new IcerikFabrikasiKillSwitch(false, "t", "OP"), false);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveRenderStatus.BLOCKED_BY_OPERATOR_GATE);
            assertThat(r.isOperatorGatePassed()).isFalse();
        }
    }

    // D. All Gates Passed — Real FFmpeg
    @Nested @DisplayName("D. All Gates Passed — Real FFmpeg")
    class AllGates {
        @Test @DisplayName("REAL_RENDER_AVAILABLE_INTERNAL")
        void realResult() {
            var r = service.renderReal(fakeStorage(), new IcerikFabrikasiKillSwitch(false, "t", "OP"), true);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveRenderStatus.REAL_RENDER_AVAILABLE_INTERNAL);
            assertThat(r.getMode()).isEqualTo(IcerikFabrikasiLiveRenderMode.LOCAL_MANUAL_REAL_FFMPEG);
            assertThat(r.getRenderArtifact()).isNotNull();
            assertThat(r.getRenderArtifact().getOpaqueRenderRef()).startsWith("rrnd:");
        }
        @Test @DisplayName("render artifact requires human review")
        void humanReview() {
            var r = service.renderReal(fakeStorage(), new IcerikFabrikasiKillSwitch(false, "t", "OP"), true);
            assertThat(r.getRenderArtifact().isHumanReviewRequired()).isTrue();
        }
    }

    // E. Render Artifact Verification
    @Nested @DisplayName("E. Render Artifact Verification")
    class Verification {
        @Test @DisplayName("fake render artifact verified")
        void fakeVerified() {
            var r = service.renderFake(fakeStorage());
            assertThat(r.getRenderArtifact().isVerified()).isTrue();
            assertThat(r.getRenderArtifact().isHumanReviewRequired()).isFalse();
        }
        @Test @DisplayName("real render artifact verified + review required")
        void realVerified() {
            var r = service.renderReal(fakeStorage(), new IcerikFabrikasiKillSwitch(false, "t", "OP"), true);
            assertThat(r.getRenderArtifact().isVerified()).isTrue();
            assertThat(r.getRenderArtifact().isHumanReviewRequired()).isTrue();
        }
    }

    // F. Forbidden Sweep
    @Nested @DisplayName("F. Forbidden Field Sweep")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields")
        void sweep() {
            for (var c : LIVE004_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // G. No Forbidden Statuses
    @Nested @DisplayName("G. No Forbidden Statuses")
    class NoForbidden {
        @Test @DisplayName("render status safe")
        void safe() {
            for (var v : IcerikFabrikasiLiveRenderStatus.values())
                for (var f : FORBIDDEN_STATUSES)
                    assertThat(v.name()).isNotEqualTo(f);
        }
        @Test @DisplayName("7 statuses")
        void count() { assertThat(IcerikFabrikasiLiveRenderStatus.values()).hasSize(7); }
        @Test @DisplayName("2 modes")
        void modes() { assertThat(IcerikFabrikasiLiveRenderMode.values()).hasSize(2); }
        @Test @DisplayName("7 issue codes")
        void issues() { assertThat(IcerikFabrikasiLiveRenderIssueCode.values()).hasSize(7); }
    }

    // H. Endpoint Unchanged
    @Nested @DisplayName("H. Endpoint Inventory (46 = 26 POST + 20 GET)")
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

    // I. ProcessBuilder / Runtime
    @Nested @DisplayName("I. ProcessBuilder / Runtime")
    class ProcessCheck {
        @Test @DisplayName("no ProcessBuilder/Runtime in LIVE-004 classes")
        void clean() {
            for (var c : LIVE004_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }
}

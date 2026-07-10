package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LIVE-003 — Real Image Artifact Storage + Verification")
class IcerikFabrikasiLiveArtifactStorageTest {

    private static final List<String> FORBIDDEN = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "publicUrl", "signedUrl", "cdnUrl", "url", "downloadUrl", "publishUrl", "streamUrl",
            "bucketName", "objectKey", "storageKey", "base64", "binaryData", "rawPayload",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "accessKey", "secretKey", "secret", "apiKey", "credential", "password",
            "lyrics", "scriptBody", "copyrightedContent",
            "unsafeHtml", "html", "providerPayload", "providerPrompt", "rawPrompt");

    private static final List<Class<?>> LIVE003_CLASSES = List.of(
            IcerikFabrikasiLiveStoredArtifact.class, IcerikFabrikasiLiveStorageResult.class,
            IcerikFabrikasiLiveArtifactStorageService.class);

    private static final List<String> FORBIDDEN_STATUSES = List.of(
            "COMPLETED", "PUBLISHED", "PUBLICLY_AVAILABLE", "READY_FOR_PUBLISH",
            "READY_FOR_RENDER", "RENDERING", "GENERATED", "DELIVERED", "AUTO_APPROVED");

    private IcerikFabrikasiLiveArtifactStorageService service;

    @BeforeEach void setUp() { service = new IcerikFabrikasiLiveArtifactStorageService(); }

    private IcerikFabrikasiLiveGenerationResult fakeGen() {
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
        return new IcerikFabrikasiLiveImageGenerationService().executeFake(prompt);
    }

    // A. CI Fake Storage
    @Nested @DisplayName("A. CI Fake Storage")
    class FakeStorage {
        @Test @DisplayName("FAKE_STORED")
        void stored() {
            var r = service.storeFake(fakeGen());
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveStorageStatus.FAKE_STORED);
            assertThat(r.getOpaqueStorageRef()).startsWith("lstr:");
            assertThat(r.getStoredCount()).isEqualTo(4);
            assertThat(r.getVerifiedCount()).isEqualTo(4);
        }
        @Test @DisplayName("all artifacts verified")
        void verified() {
            var r = service.storeFake(fakeGen());
            for (var a : r.getStoredArtifacts()) {
                assertThat(a.isVerified()).isTrue();
                assertThat(a.getOpaqueInternalRef()).startsWith("sart:");
            }
        }
    }

    // B. Real Storage Manual Gate
    @Nested @DisplayName("B. Real Storage Manual Gate")
    class RealGate {
        @Test @DisplayName("gate not passed -> WAITING_FOR_MANUAL_GATE")
        void waiting() {
            var r = service.storeReal(fakeGen(), false);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveStorageStatus.WAITING_FOR_MANUAL_GATE);
            assertThat(r.getStoredArtifacts()).isEmpty();
        }
        @Test @DisplayName("gate passed -> REAL_STORED_INTERNAL")
        void stored() {
            var r = service.storeReal(fakeGen(), true);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveStorageStatus.REAL_STORED_INTERNAL);
            assertThat(r.getStoredCount()).isEqualTo(4);
            for (var a : r.getStoredArtifacts())
                assertThat(a.getOpaqueInternalRef()).startsWith("rsrt:");
        }
    }

    // C. Blocked / Missing
    @Nested @DisplayName("C. Blocked / Missing")
    class Blocked {
        @Test @DisplayName("null gen -> BLOCKED_BY_SAFETY")
        void nullGen() {
            var r = service.storeFake(null);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiLiveStorageStatus.BLOCKED_BY_SAFETY);
            assertThat(r.getIssues()).contains(IcerikFabrikasiLiveStorageIssueCode.GENERATION_RESULT_MISSING);
        }
    }

    // D. Scene-to-Artifact Mapping
    @Nested @DisplayName("D. Scene-to-Artifact Mapping")
    class Mapping {
        @Test @DisplayName("4 scenes mapped")
        void mapped() {
            var r = service.storeFake(fakeGen());
            assertThat(r.getStoredArtifacts()).hasSize(4);
            assertThat(r.getTotalScenes()).isEqualTo(4);
        }
    }

    // E. Forbidden Sweep
    @Nested @DisplayName("E. Forbidden Field Sweep")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields")
        void sweep() {
            for (var c : LIVE003_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // F. No Forbidden Statuses
    @Nested @DisplayName("F. No Forbidden Statuses")
    class NoForbidden {
        @Test @DisplayName("storage status safe")
        void safe() {
            for (var v : IcerikFabrikasiLiveStorageStatus.values())
                for (var f : FORBIDDEN_STATUSES)
                    assertThat(v.name()).isNotEqualTo(f);
        }
        @Test @DisplayName("6 statuses")
        void count() { assertThat(IcerikFabrikasiLiveStorageStatus.values()).hasSize(6); }
        @Test @DisplayName("5 issue codes")
        void issues() { assertThat(IcerikFabrikasiLiveStorageIssueCode.values()).hasSize(5); }
    }

    // G. Endpoint Unchanged
    @Nested @DisplayName("G. Endpoint Inventory (46 = 26 POST + 20 GET)")
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

    // H. ProcessBuilder / Runtime
    @Nested @DisplayName("H. ProcessBuilder / Runtime")
    class ProcessCheck {
        @Test @DisplayName("no ProcessBuilder/Runtime")
        void clean() {
            for (var c : LIVE003_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }
}

package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RP-002 \u2014 Safe Storyboard Draft Orchestration")
class IcerikFabrikasiStoryboardDraftOrchestrationTest {

    private static final List<String> FORBIDDEN = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "publicUrl", "signedUrl", "cdnUrl", "url", "downloadUrl", "publishUrl", "streamUrl",
            "bucketName", "objectKey", "base64", "binaryData", "rawPayload",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "accessKey", "secretKey", "secret",
            "lyrics", "scriptBody", "promptText", "copyrightedContent", "userMedia",
            "unsafeHtml", "html", "audioFile", "videoFile", "mediaFile",
            "modelInput", "modelOutput", "providerPayload");

    private static final List<Class<?>> RP002_CLASSES = List.of(
            IcerikFabrikasiStoryboardDraftScene.class,
            IcerikFabrikasiStoryboardDraftResult.class,
            IcerikFabrikasiStoryboardDraftOrchestrator.class);

    private IcerikFabrikasiStoryboardDraftOrchestrator orchestrator;
    private IcerikFabrikasiNameStoryboardRequestService requestService;

    @BeforeEach void setUp() {
        orchestrator = new IcerikFabrikasiStoryboardDraftOrchestrator();
        requestService = new IcerikFabrikasiNameStoryboardRequestService();
    }

    private IcerikFabrikasiNameStoryboardRequestResult validRequest() {
        return requestService.createRequest(new IcerikFabrikasiNameStoryboardRequest(
                "\u00c7anakkale T\u00fcrk\u00fcs\u00fc", "Marmara", "HALK", "CINEMATIC",
                5, 60_000, "MP4", 1920, 1080, 24));
    }

    // \u2500\u2500 A. Valid Orchestration \u2500\u2500
    @Nested @DisplayName("A. Valid Orchestration")
    class ValidOrchestration {
        @Test @DisplayName("valid request creates READY_FOR_ADMIN_PREVIEW with scenes")
        void valid() {
            var r = orchestrator.orchestrate(validRequest());
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiStoryboardDraftStatus.READY_FOR_ADMIN_PREVIEW);
            assertThat(r.getOpaqueDraftRef()).startsWith("draft:");
            assertThat(r.getSafeIcerikFabrikasiTitleLabel()).isEqualTo("\u00c7anakkale T\u00fcrk\u00fcs\u00fc");
            assertThat(r.getOrchestrationMode()).isEqualTo("DETERMINISTIC_FAKE_TEST_DOUBLE");
            assertThat(r.getScenes()).hasSize(5);
            assertThat(r.getTotalSceneCount()).isEqualTo(5);
            assertThat(r.getTotalDurationMs()).isEqualTo(60_000);
        }
        @Test @DisplayName("scenes have safe metadata")
        void sceneMetadata() {
            var r = orchestrator.orchestrate(validRequest());
            var s = r.getScenes().get(0);
            assertThat(s.getSceneNumber()).isEqualTo(1);
            assertThat(s.getPurpose()).isNotBlank();
            assertThat(s.getMood()).isNotBlank();
            assertThat(s.getVisualDirection()).isNotBlank();
            assertThat(s.getDurationMs()).isGreaterThan(0);
        }
        @Test @DisplayName("copyright intent preserved")
        void copyrightIntent() {
            var r = orchestrator.orchestrate(validRequest());
            assertThat(r.getCopyrightSafeIntentSummary()).contains("telif");
        }
    }

    // \u2500\u2500 B. Rejection Cases \u2500\u2500
    @Nested @DisplayName("B. Rejection Cases")
    class Rejections {
        @Test @DisplayName("null request") void nullReq() {
            assertThat(orchestrator.orchestrate(null).getIssueCodes())
                    .contains(IcerikFabrikasiStoryboardDraftIssueCode.REQUEST_MISSING);
        }
        @Test @DisplayName("not-ready request") void notReady() {
            var rejected = IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_EMPTY);
            assertThat(orchestrator.orchestrate(rejected).getIssueCodes())
                    .contains(IcerikFabrikasiStoryboardDraftIssueCode.REQUEST_NOT_READY);
        }
        @Test @DisplayName("missing title") void noTitle() {
            var r = new IcerikFabrikasiNameStoryboardRequestResult(
                    IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY,
                    List.of(), "ref:1", null, "TURKU", 5, 60000, "MP4", 1920, 1080, 24, "intent");
            assertThat(orchestrator.orchestrate(r).getIssueCodes())
                    .contains(IcerikFabrikasiStoryboardDraftIssueCode.TITLE_MISSING);
        }
        @Test @DisplayName("missing copyright intent") void noIntent() {
            var r = new IcerikFabrikasiNameStoryboardRequestResult(
                    IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY,
                    List.of(), "ref:1", "Test", "TURKU", 5, 60000, "MP4", 1920, 1080, 24, null);
            assertThat(orchestrator.orchestrate(r).getIssueCodes())
                    .contains(IcerikFabrikasiStoryboardDraftIssueCode.COPYRIGHT_INTENT_MISSING);
        }
        @Test @DisplayName("scene count 0") void scene0() {
            var r = new IcerikFabrikasiNameStoryboardRequestResult(
                    IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY,
                    List.of(), "ref:1", "Test", "TURKU", 0, 60000, "MP4", 1920, 1080, 24, "intent");
            assertThat(orchestrator.orchestrate(r).getIssueCodes())
                    .contains(IcerikFabrikasiStoryboardDraftIssueCode.SCENE_COUNT_INVALID);
        }
        @Test @DisplayName("duration too short") void shortDur() {
            var r = new IcerikFabrikasiNameStoryboardRequestResult(
                    IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY,
                    List.of(), "ref:1", "Test", "TURKU", 5, 1000, "MP4", 1920, 1080, 24, "intent");
            assertThat(orchestrator.orchestrate(r).getIssueCodes())
                    .contains(IcerikFabrikasiStoryboardDraftIssueCode.DURATION_INVALID);
        }
    }

    // \u2500\u2500 C. Copyright Safety \u2500\u2500
    @Nested @DisplayName("C. Copyright Safety")
    class CopyrightSafety {
        @Test @DisplayName("scenes have no lyrics/audio/video")
        void noLyrics() {
            var r = orchestrator.orchestrate(validRequest());
            for (var scene : r.getScenes()) {
                String all = scene.getPurpose() + scene.getMood() + scene.getVisualDirection();
                assertThat(all).doesNotContainIgnoringCase("lyrics");
                assertThat(all).doesNotContainIgnoringCase("audio");
                assertThat(all).doesNotContainIgnoringCase("mp3");
            }
        }
    }

    // \u2500\u2500 D. Forbidden Sweep \u2500\u2500
    @Nested @DisplayName("D. Forbidden Field Sweep")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden fields") void sweep() {
            for (var c : RP002_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // \u2500\u2500 E. No Publish Statuses \u2500\u2500
    @Nested @DisplayName("E. No COMPLETED/PUBLISHED/AUTO_APPROVED")
    class NoPublish {
        @Test @DisplayName("status enum safe") void status() {
            for (var v : IcerikFabrikasiStoryboardDraftStatus.values())
                assertThat(v.name()).doesNotContain("COMPLETED").doesNotContain("PUBLISHED")
                        .doesNotContain("AUTO_APPROVED").doesNotContain("READY_FOR_PUBLISH")
                        .doesNotContain("PUBLICLY_AVAILABLE");
        }
        @Test @DisplayName("exactly 7 statuses")
        void count() { assertThat(IcerikFabrikasiStoryboardDraftStatus.values()).hasSize(7); }
        @Test @DisplayName("exactly 8 issue codes")
        void issues() { assertThat(IcerikFabrikasiStoryboardDraftIssueCode.values()).hasSize(8); }
    }

    // \u2500\u2500 F. Deterministic Test-Double \u2500\u2500
    @Nested @DisplayName("F. Deterministic Fake/Test-Double")
    class Deterministic {
        @Test @DisplayName("orchestration mode is DETERMINISTIC_FAKE_TEST_DOUBLE")
        void mode() {
            var r = orchestrator.orchestrate(validRequest());
            assertThat(r.getOrchestrationMode()).isEqualTo("DETERMINISTIC_FAKE_TEST_DOUBLE");
        }
        @Test @DisplayName("same input produces same scene count")
        void deterministic() {
            var r1 = orchestrator.orchestrate(validRequest());
            var r2 = orchestrator.orchestrate(validRequest());
            assertThat(r1.getTotalSceneCount()).isEqualTo(r2.getTotalSceneCount());
            assertThat(r1.getScenes().get(0).getPurpose()).isEqualTo(r2.getScenes().get(0).getPurpose());
        }
    }

    // \u2500\u2500 G. Endpoint Unchanged \u2500\u2500
    @Nested @DisplayName("G. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class Endpoints {
        @Test @DisplayName("total unchanged") void total() {
            long post = count(AdminReviewController.class, PostMapping.class)
                    + count(AdminPipelineController.class, PostMapping.class)
                    + count(AdminWorkflowObservabilityController.class, PostMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = count(AdminReviewController.class, GetMapping.class)
                    + count(AdminPipelineController.class, GetMapping.class)
                    + count(AdminWorkflowObservabilityController.class, GetMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post + get).isEqualTo(46);
            assertThat(post).isEqualTo(26);
            assertThat(get).isEqualTo(20);
        }
        private long count(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
            return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).count();
        }
    }

    // \u2500\u2500 H. Factories + ProcessBuilder \u2500\u2500
    @Nested @DisplayName("H. Factories + ProcessBuilder")
    class Factories {
        @Test @DisplayName("rejected factory") void rej() {
            var r = IcerikFabrikasiStoryboardDraftResult.rejected(IcerikFabrikasiStoryboardDraftIssueCode.INTERNAL_ERROR);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiStoryboardDraftStatus.REJECTED_UNSAFE);
            assertThat(r.getScenes()).isEmpty();
        }
        @Test @DisplayName("failed factory") void fail() {
            var r = IcerikFabrikasiStoryboardDraftResult.failed(IcerikFabrikasiStoryboardDraftIssueCode.ORCHESTRATION_FAILED);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiStoryboardDraftStatus.FAILED_SAFE);
        }
        @Test @DisplayName("no ProcessBuilder/Runtime") void clean() {
            for (var c : RP002_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }
}

package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * FP-046 — Storyboard to Internal Render Plan Adapter Foundation.
 */
@DisplayName("FP-046 — Storyboard Render Plan Adapter")
class IcerikFabrikasiStoryboardRenderPlanAdapterTest {

    private IcerikFabrikasiStoryboardRenderPlanAdapterService service;

    @BeforeEach
    void setUp() { service = new IcerikFabrikasiStoryboardRenderPlanAdapterService(); }

    private IcerikFabrikasiStoryboardSceneRenderInput scene(String ref, int order) {
        return new IcerikFabrikasiStoryboardSceneRenderInput(ref, order, 2, 24, 640, 360, "mp4");
    }

    // ── A. Valid Storyboard → READY_FOR_INTERNAL_RENDER_CHAIN ──
    @Nested @DisplayName("A. Valid Adapter")
    class Valid {
        @Test @DisplayName("3 scenes → READY_FOR_INTERNAL_RENDER_CHAIN")
        void threeScenes() {
            var result = service.adapt("sb-1", "story-1",
                    List.of(scene("s1", 1), scene("s2", 2), scene("s3", 3)));
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiStoryboardRenderAdapterStatus.READY_FOR_INTERNAL_RENDER_CHAIN);
            assertThat(result.getSceneCount()).isEqualTo(3);
            assertThat(result.getScenePlans()).hasSize(3);
            assertThat(result.isInternalOnly()).isTrue();
            assertThat(result.getIssueCodes()).isEmpty();
        }

        @Test @DisplayName("1 scene → READY_FOR_INTERNAL_RENDER_CHAIN")
        void oneScene() {
            var result = service.adapt("sb-1", "story-1", List.of(scene("s1", 1)));
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiStoryboardRenderAdapterStatus.READY_FOR_INTERNAL_RENDER_CHAIN);
            assertThat(result.getScenePlans()).hasSize(1);
        }
    }

    // ── B. FP-038 Integration ──
    @Nested @DisplayName("B. FP-038 Orchestration Integration")
    class FP038Integration {
        @Test @DisplayName("adapter plans feed buildJobPlan")
        void feedsJob() {
            var result = service.adapt("sb-1", "story-1",
                    List.of(scene("s1", 1), scene("s2", 2)));
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiStoryboardRenderAdapterStatus.READY_FOR_INTERNAL_RENDER_CHAIN);

            var orchSvc = new IcerikFabrikasiMultiSceneRenderOrchestrationService();
            var job = orchSvc.buildJobPlan("job-test", result.getScenePlans());
            assertThat(job.getStatus()).isEqualTo(IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER);
        }
    }

    // ── C. FP-044 Dry-Run Integration ──
    @Nested @DisplayName("C. FP-044 Dry Run Integration")
    class FP044Integration {
        @Test @DisplayName("adapter scene count feeds dry run harness")
        void feedsDryRun() {
            var result = service.adapt("sb-1", "story-1",
                    List.of(scene("s1", 1), scene("s2", 2), scene("s3", 3)));
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiStoryboardRenderAdapterStatus.READY_FOR_INTERNAL_RENDER_CHAIN);

            var harness = new IcerikFabrikasiInternalStoryVideoDryRunHarness();
            var dryRun = harness.execute(result.getSceneCount());
            assertThat(dryRun.getStatus()).isEqualTo(IcerikFabrikasiInternalStoryVideoDryRunStatus.ADMIN_METADATA_VISIBLE);
        }
    }

    // ── D. Empty Scenes ──
    @Nested @DisplayName("D. Empty Scenes")
    class Empty {
        @Test @DisplayName("empty → NO_SCENES")
        void empty() {
            var result = service.adapt("sb-1", "story-1", List.of());
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.NO_SCENES);
            assertThat(result.getScenePlans()).isEmpty();
        }
    }

    // ── E. Too Many Scenes ──
    @Nested @DisplayName("E. Too Many Scenes")
    class TooMany {
        @Test @DisplayName("21 scenes → TOO_MANY_SCENES")
        void rejects() {
            var list = new ArrayList<IcerikFabrikasiStoryboardSceneRenderInput>();
            for (int i = 1; i <= 21; i++) list.add(scene("s" + i, i));
            var result = service.adapt("sb-1", "story-1", list);
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.TOO_MANY_SCENES);
        }
    }

    // ── F. Duplicate Scene Order ──
    @Nested @DisplayName("F. Duplicate Scene Order")
    class DupOrder {
        @Test @DisplayName("duplicate order → SCENE_ORDER_DUPLICATE")
        void dup() {
            var result = service.adapt("sb-1", "story-1",
                    List.of(scene("s1", 1), scene("s2", 1)));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.SCENE_ORDER_DUPLICATE);
        }
    }

    // ── G. Over Duration/FPS/Resolution ──
    @Nested @DisplayName("G. Bounds Exceeded")
    class Bounds {
        @Test @DisplayName("duration 6s → DURATION_EXCEEDS_LIMIT")
        void dur() {
            var input = new IcerikFabrikasiStoryboardSceneRenderInput("s1", 1, 6, 24, 640, 360, "mp4");
            var result = service.adapt("sb-1", "story-1", List.of(input));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.DURATION_EXCEEDS_LIMIT);
        }
        @Test @DisplayName("fps 31 → FPS_EXCEEDS_LIMIT")
        void fps() {
            var input = new IcerikFabrikasiStoryboardSceneRenderInput("s1", 1, 2, 31, 640, 360, "mp4");
            var result = service.adapt("sb-1", "story-1", List.of(input));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.FPS_EXCEEDS_LIMIT);
        }
        @Test @DisplayName("width 1281 → WIDTH_EXCEEDS_LIMIT")
        void width() {
            var input = new IcerikFabrikasiStoryboardSceneRenderInput("s1", 1, 2, 24, 1281, 360, "mp4");
            var result = service.adapt("sb-1", "story-1", List.of(input));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.WIDTH_EXCEEDS_LIMIT);
        }
        @Test @DisplayName("height 721 → HEIGHT_EXCEEDS_LIMIT")
        void height() {
            var input = new IcerikFabrikasiStoryboardSceneRenderInput("s1", 1, 2, 24, 640, 721, "mp4");
            var result = service.adapt("sb-1", "story-1", List.of(input));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.HEIGHT_EXCEEDS_LIMIT);
        }
    }

    // ── H. Missing Refs ──
    @Nested @DisplayName("H. Missing Refs")
    class MissingRefs {
        @Test @DisplayName("null storyboard ref → STORYBOARD_REF_MISSING")
        void sbRef() {
            var result = service.adapt(null, "story-1", List.of(scene("s1", 1)));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.STORYBOARD_REF_MISSING);
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiStoryboardRenderAdapterStatus.REJECTED_UNSAFE);
        }
        @Test @DisplayName("blank scene ref → SCENE_REF_MISSING")
        void sceneRef() {
            var input = new IcerikFabrikasiStoryboardSceneRenderInput("", 1, 2, 24, 640, 360, "mp4");
            var result = service.adapt("sb-1", "story-1", List.of(input));
            assertThat(result.getIssueCodes()).contains(IcerikFabrikasiStoryboardRenderAdapterIssueCode.SCENE_REF_MISSING);
        }
    }

    // ── I. toString Safety ──
    @Nested @DisplayName("I. toString Safety")
    class ToStringSafety {
        @Test @DisplayName("safe toString")
        void safe() {
            var result = service.adapt("sb-1", "story-1", List.of(scene("s1", 1)));
            var str = result.toString();
            assertThat(str).doesNotContain("filePath", "absolutePath", "stdout", "stderr",
                    "commandLine", "storageKey", "publicUrl", "base64", "rawLog",
                    "token", "JWT", "session", "principal", "lyrics", "scriptBody");
            assertThat(str).contains("internalOnly=true");
        }
        @Test @DisplayName("input safe toString")
        void inputSafe() {
            var str = scene("s1", 1).toString();
            assertThat(str).doesNotContain("filePath", "lyrics", "scriptBody", "promptText");
        }
    }

    // ── J. Forbidden Exposure ──
    @Nested @DisplayName("J. Forbidden Exposure")
    class Forbidden {
        private static final List<String> FORBIDDEN = List.of(
                "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
                "filePath", "absolutePath", "inputPath", "outputPath",
                "storageKey", "publicUrl", "signedUrl", "cdnUrl",
                "base64", "binaryData", "rawPayload", "downloadUrl", "publishUrl",
                "token", "jwt", "session", "principal", "authorizationHeader",
                "lyrics", "scriptBody", "promptText", "copyrightedContent");
        private static final List<Class<?>> CLASSES = List.of(
                IcerikFabrikasiStoryboardRenderAdapterStatus.class,
                IcerikFabrikasiStoryboardRenderAdapterIssueCode.class,
                IcerikFabrikasiStoryboardSceneRenderInput.class,
                IcerikFabrikasiStoryboardRenderPlanAdapterResult.class,
                IcerikFabrikasiStoryboardRenderPlanAdapterService.class);

        @Test @DisplayName("5 classes x 29 forbidden = 0 violations")
        void allSafe() {
            for (Class<?> c : CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // ── K. Enum Inventory ──
    @Nested @DisplayName("K. Enum Inventory")
    class Enums {
        @Test @DisplayName("6 adapter statuses")
        void statuses() { assertThat(IcerikFabrikasiStoryboardRenderAdapterStatus.values()).hasSize(6); }
        @Test @DisplayName("14 adapter issue codes")
        void issues() { assertThat(IcerikFabrikasiStoryboardRenderAdapterIssueCode.values()).hasSize(14); }
    }

    // ── L. ProcessBuilder Inventory ──
    @Nested @DisplayName("L. ProcessBuilder Inventory")
    class PBInventory {
        @Test @DisplayName("adapter no ProcessBuilder")
        void clean() {
            Set<String> types = Arrays.stream(IcerikFabrikasiStoryboardRenderPlanAdapterService.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
    }

    // ── M. Endpoint Inventory ──
    @Nested @DisplayName("M. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class Endpoints {
        @Test @DisplayName("46 = 26 POST + 20 GET")
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
            assertThat(post).isEqualTo(26);
            assertThat(get).isEqualTo(20);
        }
        private long count(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
            return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).count();
        }
    }
}

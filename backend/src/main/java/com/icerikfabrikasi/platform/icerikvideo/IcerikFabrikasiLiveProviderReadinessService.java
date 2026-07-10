package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;

/**
 * LIVE-001: Evaluates provider credential presence and readiness.
 * No real provider call. Credential presence status only.
 */
public class IcerikFabrikasiLiveProviderReadinessService {

    private static final List<String> PREREQUISITES = List.of(
            "Kimlik bilgisi mevcut olmali",
            "Operator tarafindan etkinlestirilmis olmali",
            "Kill switch devre disi olmali",
            "RPE yurutme paketi hazir olmali",
            "Gorsel prompt paketi onaylanmis olmali");

    /**
     * Evaluate provider readiness.
     * @param credentialPresent whether a credential is configured (presence only, no value)
     * @param operatorEnabled whether the operator explicitly enabled the provider
     * @param killSwitch kill switch state
     */
    public IcerikFabrikasiLiveProviderReadinessReport evaluate(
            boolean credentialPresent, boolean operatorEnabled, IcerikFabrikasiKillSwitch killSwitch) {

        if (killSwitch == null) killSwitch = IcerikFabrikasiKillSwitch.defaultActive();

        List<IcerikFabrikasiLiveProviderIssueCode> issues = new ArrayList<>();
        List<String> blocked = new ArrayList<>();

        if (killSwitch.isActive()) {
            issues.add(IcerikFabrikasiLiveProviderIssueCode.KILL_SWITCH_ACTIVE);
            blocked.add("Kill switch aktif: " + killSwitch.getReason());
        }
        if (!credentialPresent) {
            issues.add(IcerikFabrikasiLiveProviderIssueCode.CREDENTIAL_NOT_PRESENT);
            blocked.add("Kimlik bilgisi mevcut degil");
        }
        if (!operatorEnabled) {
            issues.add(IcerikFabrikasiLiveProviderIssueCode.OPERATOR_ENABLEMENT_REQUIRED);
            blocked.add("Operator tarafindan etkinlestirilmedi");
        }

        IcerikFabrikasiLiveProviderStatus status;
        if (killSwitch.isActive()) {
            status = IcerikFabrikasiLiveProviderStatus.BLOCKED_BY_KILL_SWITCH;
        } else if (!credentialPresent) {
            status = IcerikFabrikasiLiveProviderStatus.DISABLED_BY_DEFAULT;
        } else if (!operatorEnabled) {
            status = IcerikFabrikasiLiveProviderStatus.WAITING_FOR_OPERATOR_ENABLEMENT;
        } else {
            status = IcerikFabrikasiLiveProviderStatus.CREDENTIAL_PRESENT;
        }

        String credLabel = credentialPresent ? "Kimlik bilgisi mevcut (deger gizli)" : "Kimlik bilgisi mevcut degil";

        return new IcerikFabrikasiLiveProviderReadinessReport(
                status, issues,
                credentialPresent, credLabel,
                operatorEnabled, killSwitch,
                PREREQUISITES, blocked,
                "OpenAI-uyumlu gorsel saglayici",
                String.format("Dogrulama: %d sorun, saglayici %s",
                        issues.size(), status == IcerikFabrikasiLiveProviderStatus.CREDENTIAL_PRESENT ? "hazir" : "hazir degil"));
    }
}

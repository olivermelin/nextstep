import { OnboardingData, OnboardingResponse } from "@/types/onboarding";
import { API_ENDPOINTS, fetchWithCredentials } from "@/config/api";

export const onboardingApi = {
  // Slutför onboarding och spara användardata
  completeOnboarding: async (
    userId: string,
    data: OnboardingData
  ): Promise<OnboardingResponse> => {
    return fetchWithCredentials(API_ENDPOINTS.ONBOARDING.COMPLETE(userId), {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // Kontrollera om användaren har slutfört onboarding
  checkOnboardingStatus: async (userId: string): Promise<{ completed: boolean }> => {
    return fetchWithCredentials(API_ENDPOINTS.ONBOARDING.STATUS(userId));
  },
};

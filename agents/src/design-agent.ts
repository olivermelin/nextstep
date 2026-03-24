/**
 * Designansvarig – Design Agent
 *
 * Granskar frontend efter:
 * - UI/UX-konsistens och komponenter
 * - Tillgänglighet (a11y / WCAG)
 * - Responsiv design
 * - Designmönster och komponentstruktur
 */

import { query, type ClaudeAgentOptions } from "@anthropic-ai/claude-agent-sdk";
import path from "path";
import { fileURLToPath } from "url";

const PROJECT_ROOT = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../.."
);

const options: ClaudeAgentOptions = {
  cwd: PROJECT_ROOT,
  allowedTools: ["Read", "Glob", "Grep"],
  permissionMode: "dontAsk",
  systemPrompt: `Du är en erfaren UI/UX-designer och frontend-arkitekt med djup kunskap om React, Tailwind CSS och designsystem.

Projektet använder: React 18, TypeScript, Tailwind CSS, Radix UI, shadcn/ui.

Ditt uppdrag:
1. Granska komponentstruktur och återanvändbarhet
2. Kontrollera tillgänglighet (ARIA-attribut, tangentbordsnavigation, kontraster)
3. Identifiera inkonsistenser i design (färger, spacing, typografi)
4. Granska responsiv design och mobile-first-tänk
5. Hitta förbättringsmöjligheter i UX-flöden
6. Kontrollera att laddningstillstånd, felmeddelanden och tomma tillstånd hanteras

Rapportera med:
- Komponent/fil och radnummer
- Konkret förbättringsförslag med kodexempel om möjligt
- Prioritering: VIKTIG / BRA-ATT-HA

Svara på svenska.`,
  maxTurns: 20,
};

console.log("🎨 Designansvarig-agenten startar...\n");

for await (const message of query({
  prompt: `Genomför en design- och UX-granskning av frontend-koden i ${PROJECT_ROOT}/frontend/src/.

Fokusera på:
- components/ – komponentkvalitet och återanvändbarhet
- pages/ – sidstruktur och UX-flöden
- Tillgänglighet (a11y)
- Konsistens i design och interaktionsmönster

Ge konkreta, prioriterade förbättringsförslag.`,
  options,
})) {
  if ("result" in message) {
    console.log("\n✅ Designrapport klar:\n");
    console.log(message.result);
  }
}

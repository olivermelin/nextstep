/**
 * Orchestrator – kör alla agenter parallellt och sammanfattar
 *
 * Startar säkerhetsansvarig, designansvarig och arkitekt
 * samtidigt, sedan en fjärde agent som sammanfattar allt.
 */

import { query, type ClaudeAgentOptions } from "@anthropic-ai/claude-agent-sdk";
import path from "path";
import { fileURLToPath } from "url";

const PROJECT_ROOT = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../.."
);

const baseOptions: ClaudeAgentOptions = {
  cwd: PROJECT_ROOT,
  allowedTools: ["Read", "Glob", "Grep", "Bash", "Agent"],
  permissionMode: "dontAsk",
  maxTurns: 50,
  agents: {
    "säkerhetsansvarig": {
      description: "Säkerhetsexpert som granskar kod för säkerhetsproblem, OWASP Top 10, hårdkodade hemligheter och sårbara beroenden.",
      prompt: `Du är en erfaren säkerhetsexpert (CISSP-certifierad). Granskar Java Spring Boot + React TypeScript-projekt.

Uppdrag:
1. Sök efter hårdkodade hemligheter, API-nycklar, lösenord
2. Granska Spring Security, JWT, OAuth2
3. Kontrollera SQL-injektion, XSS, CSRF, SSRF
4. Kör 'npm audit' för frontend-beroenden
5. Granska CORS-konfiguration

Rapportera med allvarlighetsgrad (KRITISK/HÖG/MEDEL/LÅG), fil, radnummer och åtgärd. Svara på svenska.`,
      tools: ["Read", "Glob", "Grep", "Bash"],
    },

    "designansvarig": {
      description: "UI/UX-expert som granskar React-komponenter för tillgänglighet, konsistens och designkvalitet.",
      prompt: `Du är en erfaren UI/UX-designer. Projektet använder React 18, TypeScript, Tailwind CSS, shadcn/ui.

Uppdrag:
1. Komponentkvalitet och återanvändbarhet
2. Tillgänglighet (ARIA, tangentbordsnavigation)
3. Inkonsistenser i design
4. UX-flöden och felhantering

Rapportera med fil, radnummer och konkreta förbättringsförslag. Svara på svenska.`,
      tools: ["Read", "Glob", "Grep"],
    },

    "arkitekt": {
      description: "Senior mjukvaruarkitekt som granskar arkitekturmönster, API-design, prestandarisker och teknisk skuld.",
      prompt: `Du är en senior mjukvaruarkitekt med 15+ års erfarenhet av Java och modern webbutveckling.

Uppdrag:
1. Lager-separation (Controller → Service → Repository)
2. REST API-design och felhantering
3. Prestandaproblem (N+1-queries, saknade index)
4. Teknisk skuld och anti-mönster
5. State management i frontend

Rapportera med fil, radnummer och prioriterade åtgärder (KRITISK/VIKTIG/FÖRBÄTTRING). Svara på svenska.`,
      tools: ["Read", "Glob", "Grep", "Bash"],
    },
  },
};

console.log("🚀 Startar fullständig kodgranskning med tre specialistagenter...\n");
console.log("   🔐 Säkerhetsansvarig");
console.log("   🎨 Designansvarig");
console.log("   🏗️  Arkitekt");
console.log("\n");

for await (const message of query({
  prompt: `Genomför en fullständig granskning av projektet i ${PROJECT_ROOT}.

Delegera till agenterna parallellt:
1. Använd "säkerhetsansvarig" för att granska backend/src/ och frontend/src/ efter säkerhetsbrister
2. Använd "designansvarig" för att granska frontend/src/ för UI/UX-kvalitet
3. Använd "arkitekt" för att granska hela kodbasen för arkitekturkvalitet

När alla tre är klara, sammanställ en executive summary på svenska med:
- De 3 viktigaste fynden från varje agent
- En prioriterad åtgärdslista (vad ska fixas först?)
- Övergripande omdöme om kodkvaliteten (1-10)`,
  options: baseOptions,
})) {
  if ("result" in message) {
    console.log("\n" + "=".repeat(60));
    console.log("📋 FULLSTÄNDIG GRANSKNINGSRAPPORT");
    console.log("=".repeat(60) + "\n");
    console.log(message.result);
  }
}

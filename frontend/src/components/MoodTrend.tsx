import { useState, useEffect } from 'react';
import { Card } from "@/components/ui/card";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { API_ENDPOINTS } from "@/config/api";
import { useTranslation } from 'react-i18next';

interface MoodEntry {
  moodScore: number;
  checkInDate: string;
}

interface MoodTrendProps {
  userId: string;
}

const MoodTrend = ({ userId }: MoodTrendProps) => {
  const { t } = useTranslation();
  const [moodData, setMoodData] = useState<MoodEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(API_ENDPOINTS.CHECKINS.GET_HISTORY(userId, 14), { credentials: "include" })
      .then(res => res.ok ? res.json() : [])
      .then((data: MoodEntry[]) => {
        // Sortera kronologiskt
        const sorted = data
          .filter((d: MoodEntry) => d.moodScore > 0)
          .sort((a: MoodEntry, b: MoodEntry) => a.checkInDate.localeCompare(b.checkInDate));
        setMoodData(sorted);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading || moodData.length < 3) return null;

  const scores = moodData.map(d => d.moodScore);
  const avg = scores.reduce((a, b) => a + b, 0) / scores.length;
  const recentAvg = scores.slice(-3).reduce((a, b) => a + b, 0) / Math.min(scores.length, 3);
  const olderAvg = scores.slice(0, -3).length > 0
    ? scores.slice(0, -3).reduce((a, b) => a + b, 0) / scores.slice(0, -3).length
    : avg;

  const trend = recentAvg - olderAvg;
  const TrendIcon = trend > 0.3 ? TrendingUp : trend < -0.3 ? TrendingDown : Minus;
  const trendColor = trend > 0.3 ? "text-green-500" : trend < -0.3 ? "text-red-400" : "text-muted-foreground";
  const trendLabel = trend > 0.3
    ? t('moodTrend.improving', { defaultValue: 'Uppåtgående' })
    : trend < -0.3
      ? t('moodTrend.declining', { defaultValue: 'Nedåtgående' })
      : t('moodTrend.stable', { defaultValue: 'Stabil' });

  // SVG sparkline
  const width = 200;
  const height = 48;
  const padding = 4;
  const stepX = (width - padding * 2) / (scores.length - 1);
  const minY = 1;
  const maxY = 5;
  const scaleY = (v: number) => height - padding - ((v - minY) / (maxY - minY)) * (height - padding * 2);

  const points = scores.map((s, i) => `${padding + i * stepX},${scaleY(s)}`).join(" ");
  const fillPoints = `${padding},${height - padding} ${points} ${padding + (scores.length - 1) * stepX},${height - padding}`;

  return (
    <Card className="p-4 bg-gradient-to-br from-card to-muted/20">
      <div className="flex items-center justify-between mb-2">
        <p className="text-sm font-semibold text-foreground">
          {t('moodTrend.title', { defaultValue: 'Humörtrend' })}
        </p>
        <div className={`flex items-center gap-1 text-xs ${trendColor}`}>
          <TrendIcon className="w-3.5 h-3.5" />
          <span>{trendLabel}</span>
        </div>
      </div>

      <svg width="100%" viewBox={`0 0 ${width} ${height}`} className="overflow-visible">
        {/* Fyllning under linjen */}
        <polygon
          points={fillPoints}
          className="fill-primary/10"
        />
        {/* Linje */}
        <polyline
          points={points}
          fill="none"
          className="stroke-primary"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {/* Sista punkten */}
        <circle
          cx={padding + (scores.length - 1) * stepX}
          cy={scaleY(scores[scores.length - 1])}
          r="3"
          className="fill-primary"
        />
      </svg>

      <div className="flex justify-between text-[10px] text-muted-foreground mt-1">
        <span>{t('moodTrend.daysAgo', { days: moodData.length, defaultValue: '{{days}} dagar sedan' })}</span>
        <span>{t('moodTrend.today', { defaultValue: 'Idag' })}</span>
      </div>
      <p className="text-xs text-muted-foreground mt-1.5">
        {t('moodTrend.average', { avg: avg.toFixed(1), defaultValue: 'Snitt: {{avg}}/5' })}
      </p>
    </Card>
  );
};

export default MoodTrend;

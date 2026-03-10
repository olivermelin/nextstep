import { useRef, useState, useEffect, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Slider } from "@/components/ui/slider";
import { Pencil, Trash2 } from "lucide-react";

const PRESET_COLORS = [
  "#000000",
  "#FFFFFF",
  "#EF4444",
  "#F97316",
  "#EAB308",
  "#22C55E",
  "#3B82F6",
  "#8B5CF6",
  "#EC4899",
  "#6B7280",
];

const DrawingCanvas = () => {
  const { t } = useTranslation();
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const contextRef = useRef<CanvasRenderingContext2D | null>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [currentColor, setCurrentColor] = useState("#000000");
  const [brushSize, setBrushSize] = useState(3);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const width = canvas.offsetWidth;
    const height = 400;
    canvas.width = width * 2;
    canvas.height = height * 2;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;

    const context = canvas.getContext("2d");
    if (!context) return;

    context.scale(2, 2);
    context.lineCap = "round";
    context.lineJoin = "round";
    context.fillStyle = "#FFFFFF";
    context.fillRect(0, 0, width, height);
    contextRef.current = context;
  }, []);

  const startDrawing = useCallback(
    (e: React.MouseEvent<HTMLCanvasElement>) => {
      const context = contextRef.current;
      if (!context) return;

      context.strokeStyle = currentColor;
      context.lineWidth = brushSize;
      context.beginPath();
      context.moveTo(e.nativeEvent.offsetX, e.nativeEvent.offsetY);
      setIsDrawing(true);
    },
    [currentColor, brushSize]
  );

  const draw = useCallback(
    (e: React.MouseEvent<HTMLCanvasElement>) => {
      if (!isDrawing) return;
      const context = contextRef.current;
      if (!context) return;

      context.lineTo(e.nativeEvent.offsetX, e.nativeEvent.offsetY);
      context.stroke();
    },
    [isDrawing]
  );

  const stopDrawing = useCallback(() => {
    const context = contextRef.current;
    if (context) {
      context.closePath();
    }
    setIsDrawing(false);
  }, []);

  const clearCanvas = useCallback(() => {
    const canvas = canvasRef.current;
    const context = contextRef.current;
    if (!canvas || !context) return;

    const width = canvas.width / 2;
    const height = canvas.height / 2;
    context.fillStyle = "#FFFFFF";
    context.fillRect(0, 0, width, height);
  }, []);

  return (
    <Card className="p-4 space-y-4">
      <div className="flex items-center gap-2 mb-2">
        <Pencil className="w-5 h-5 text-primary" />
        <h3 className="font-semibold">{t("challenges.drawing.title")}</h3>
      </div>

      <div className="border rounded-lg overflow-hidden bg-white">
        <canvas
          ref={canvasRef}
          onMouseDown={startDrawing}
          onMouseMove={draw}
          onMouseUp={stopDrawing}
          onMouseLeave={stopDrawing}
          className="cursor-crosshair w-full"
          style={{ height: "400px", touchAction: "none" }}
        />
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">
            {t("challenges.drawing.color")}
          </label>
          <div className="flex gap-1.5 flex-wrap">
            {PRESET_COLORS.map((color) => (
              <button
                key={color}
                onClick={() => setCurrentColor(color)}
                className={`w-7 h-7 rounded-full border-2 transition-all ${
                  currentColor === color
                    ? "border-primary scale-110 ring-2 ring-primary/30"
                    : "border-muted hover:scale-105"
                }`}
                style={{ backgroundColor: color }}
                aria-label={color}
              />
            ))}
          </div>
        </div>

        <div className="space-y-1 min-w-[120px]">
          <label className="text-xs text-muted-foreground">
            {t("challenges.drawing.brushSize")} ({brushSize}px)
          </label>
          <Slider
            value={[brushSize]}
            onValueChange={(val) => setBrushSize(val[0])}
            min={1}
            max={20}
            step={1}
          />
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={clearCanvas}
          className="gap-1.5 ml-auto"
        >
          <Trash2 className="w-4 h-4" />
          {t("challenges.drawing.clear")}
        </Button>
      </div>
    </Card>
  );
};

export default DrawingCanvas;

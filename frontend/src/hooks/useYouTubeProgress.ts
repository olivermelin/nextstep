import { useState, useEffect, useRef, useCallback } from "react";

declare global {
  interface Window {
    YT: any;
    onYouTubeIframeAPIReady: (() => void) | undefined;
  }
}

interface UseYouTubeProgressReturn {
  watchedPercent: number;
  isReady: boolean;
  isComplete: boolean;
  iframeRef: React.RefCallback<HTMLIFrameElement>;
}

const COMPLETION_THRESHOLD = 80; // Procent av videon som måste ses

/**
 * Tracks YouTube video watching progress using the IFrame Player API.
 * Returns isComplete when the user has watched >= 80% of the video.
 */
export function useYouTubeProgress(videoId: string | null, active: boolean): UseYouTubeProgressReturn {
  const [watchedPercent, setWatchedPercent] = useState(0);
  const [isReady, setIsReady] = useState(false);
  const playerRef = useRef<any>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const iframeElementRef = useRef<HTMLIFrameElement | null>(null);
  const maxWatchedRef = useRef(0); // Track highest point watched

  // Load YouTube IFrame API script
  useEffect(() => {
    if (!active || !videoId) return;

    if (window.YT && window.YT.Player) {
      setIsReady(true);
      return;
    }

    // Check if script is already loading
    const existingScript = document.querySelector('script[src="https://www.youtube.com/iframe_api"]');
    if (existingScript) {
      // Script exists, wait for it to load
      const checkReady = setInterval(() => {
        if (window.YT && window.YT.Player) {
          setIsReady(true);
          clearInterval(checkReady);
        }
      }, 100);
      return () => clearInterval(checkReady);
    }

    const tag = document.createElement("script");
    tag.src = "https://www.youtube.com/iframe_api";
    document.head.appendChild(tag);

    window.onYouTubeIframeAPIReady = () => {
      setIsReady(true);
    };

    return () => {
      window.onYouTubeIframeAPIReady = undefined;
    };
  }, [active, videoId]);

  // Create player when API is ready and iframe is mounted
  useEffect(() => {
    if (!isReady || !videoId || !iframeElementRef.current || !active) return;

    // Destroy existing player
    if (playerRef.current) {
      try {
        playerRef.current.destroy();
      } catch (e) {
        // ignore
      }
      playerRef.current = null;
    }

    // Reset progress
    maxWatchedRef.current = 0;
    setWatchedPercent(0);

    try {
      playerRef.current = new window.YT.Player(iframeElementRef.current, {
        events: {
          onReady: () => {
            startPolling();
          },
          onStateChange: (event: any) => {
            // YT.PlayerState.ENDED = 0
            if (event.data === 0) {
              setWatchedPercent(100);
              maxWatchedRef.current = 100;
            }
          },
        },
      });
    } catch (e) {
      console.warn("Could not initialize YouTube player:", e);
    }

    return () => {
      stopPolling();
      if (playerRef.current) {
        try {
          playerRef.current.destroy();
        } catch (e) {
          // ignore
        }
        playerRef.current = null;
      }
    };
  }, [isReady, videoId, active]);

  const startPolling = useCallback(() => {
    stopPolling();
    pollRef.current = setInterval(() => {
      if (!playerRef.current) return;

      try {
        const currentTime = playerRef.current.getCurrentTime?.();
        const duration = playerRef.current.getDuration?.();

        if (duration && duration > 0 && currentTime !== undefined) {
          const percent = Math.round((currentTime / duration) * 100);
          // Only track forward progress (prevent rewinding to cheat)
          if (percent > maxWatchedRef.current) {
            maxWatchedRef.current = percent;
          }
          setWatchedPercent(maxWatchedRef.current);
        }
      } catch (e) {
        // Player might not be ready yet
      }
    }, 2000);
  }, []);

  const stopPolling = useCallback(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }, []);

  // Ref callback to capture the iframe element
  const iframeRef = useCallback((node: HTMLIFrameElement | null) => {
    iframeElementRef.current = node;
  }, []);

  return {
    watchedPercent,
    isReady,
    isComplete: watchedPercent >= COMPLETION_THRESHOLD,
    iframeRef,
  };
}

/**
 * Extracts video ID from a YouTube URL.
 */
export function extractYouTubeVideoId(url: string | null | undefined): string | null {
  if (!url) return null;
  const match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&]+)/);
  return match ? match[1] : null;
}


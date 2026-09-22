/**
 * Mobile Permissions & Push Notifications Utility for Falcon Rod Maker POS
 * Handles:
 * 1. Web Push / System Notifications (Service Worker registration + Notification.requestPermission)
 * 2. Microphone Hardware Permission (navigator.mediaDevices.getUserMedia)
 * 3. Persistent Local Storage Permission (navigator.storage.persist + storage estimate)
 * 4. Mobile & Iframe environment diagnostics
 */

export type PermissionStateStatus = 'granted' | 'denied' | 'prompt' | 'unsupported';

export interface MobilePermissionsState {
  notifications: PermissionStateStatus;
  microphone: PermissionStateStatus;
  storagePersisted: boolean;
  storageEstimate: {
    usageMB: number;
    quotaMB: number;
    percentUsed: number;
  } | null;
  isIframe: boolean;
  isMobile: boolean;
  isServiceWorkerReady: boolean;
}

/**
 * Detect if running inside an embedded iframe (e.g. AI Studio sandboxed preview)
 */
export function isInsideIframe(): boolean {
  try {
    return window.self !== window.top;
  } catch {
    return true;
  }
}

/**
 * Detect mobile screen or handheld device
 */
export function isMobileDevice(): boolean {
  if (typeof window === 'undefined') return false;
  const ua = navigator.userAgent || '';
  const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i;
  return mobileRegex.test(ua) || window.innerWidth <= 768;
}

/**
 * Register Service Worker for background push notifications and offline caching
 */
export async function registerPosServiceWorker(): Promise<ServiceWorkerRegistration | null> {
  if (typeof window === 'undefined' || !('serviceWorker' in navigator)) {
    return null;
  }

  try {
    const reg = await navigator.serviceWorker.register('/sw.js', { scope: '/' });
    await navigator.serviceWorker.ready;
    return reg;
  } catch (err) {
    console.warn('[PWA] Service Worker registration skipped or failed:', err);
    return null;
  }
}

/**
 * Get current status of Notification permission
 */
export function getNotificationPermissionStatus(): PermissionStateStatus {
  if (typeof window === 'undefined' || !('Notification' in window)) {
    return 'unsupported';
  }
  const perm = Notification.permission;
  if (perm === 'granted') return 'granted';
  if (perm === 'denied') return 'denied';
  return 'prompt';
}

/**
 * Request Web Notification and Push permission from the user
 */
export async function requestNotificationPermission(): Promise<{
  status: PermissionStateStatus;
  message: string;
}> {
  if (typeof window === 'undefined' || !('Notification' in window)) {
    return {
      status: 'unsupported',
      message: 'Push Notifications are not supported by this mobile browser.'
    };
  }

  try {
    const permission = await Notification.requestPermission();
    // Also register service worker
    await registerPosServiceWorker();

    if (permission === 'granted') {
      return {
        status: 'granted',
        message: 'Notification permission granted! Push alerts are now active.'
      };
    } else if (permission === 'denied') {
      return {
        status: 'denied',
        message: 'Notifications blocked. You can unblock them in browser site settings.'
      };
    } else {
      return {
        status: 'prompt',
        message: 'Notification prompt was dismissed.'
      };
    }
  } catch (err) {
    return {
      status: 'prompt',
      message: `Failed to request notification permission: ${err instanceof Error ? err.message : String(err)}`
    };
  }
}

/**
 * Dispatch a native Push / System Notification
 */
export async function sendNativeNotification(
  title: string,
  options?: {
    body?: string;
    icon?: string;
    badge?: string;
    tag?: string;
    vibrate?: number[];
    data?: any;
  }
): Promise<boolean> {
  if (typeof window === 'undefined' || !('Notification' in window)) {
    return false;
  }

  if (Notification.permission !== 'granted') {
    return false;
  }

  const defaultOpts = {
    body: options?.body || 'Falcon Rod Maker POS Workshop Alert',
    icon: options?.icon || '/falcon-logo.png',
    badge: options?.badge || '/favicon.png',
    tag: options?.tag || `falcon-pos-${Date.now()}`,
    vibrate: options?.vibrate || [150, 75, 150],
    data: options?.data || { url: '/' }
  };

  // Try Service Worker registration first (standard for mobile Android & standalone PWA)
  if ('serviceWorker' in navigator) {
    try {
      const registration = await navigator.serviceWorker.getRegistration();
      if (registration && registration.showNotification) {
        await registration.showNotification(title, defaultOpts);
        return true;
      }
    } catch {
      // fallback to constructor
    }
  }

  // Fallback to desktop / standard window Notification constructor
  try {
    new Notification(title, defaultOpts);
    return true;
  } catch (e) {
    console.warn('[Notification] Direct Notification constructor failed:', e);
    return false;
  }
}

/**
 * Check Microphone permission status
 */
export async function getMicrophonePermissionStatus(): Promise<PermissionStateStatus> {
  if (typeof window === 'undefined' || !navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    return 'unsupported';
  }

  if (navigator.permissions && navigator.permissions.query) {
    try {
      const result = await navigator.permissions.query({ name: 'microphone' as PermissionName });
      if (result.state === 'granted') return 'granted';
      if (result.state === 'denied') return 'denied';
      return 'prompt';
    } catch {
      // query not supported for microphone on some mobile browsers
    }
  }

  return 'prompt';
}

/**
 * Request Microphone permission by prompting getUserMedia
 */
export async function requestMicrophonePermission(): Promise<{
  granted: boolean;
  status: PermissionStateStatus;
  message: string;
}> {
  if (typeof window === 'undefined' || !navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    return {
      granted: false,
      status: 'unsupported',
      message: 'Microphone hardware access is not supported by this browser.'
    };
  }

  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    // Stop tracks immediately so mic indicator is not left on
    stream.getTracks().forEach((track) => track.stop());
    return {
      granted: true,
      status: 'granted',
      message: 'Microphone permission granted successfully!'
    };
  } catch (err: any) {
    const isDenied = err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError';
    return {
      granted: false,
      status: isDenied ? 'denied' : 'prompt',
      message: isDenied
        ? 'Microphone access denied. Enable microphone access in browser site settings.'
        : `Microphone access error: ${err.message || err.name}`
    };
  }
}

/**
 * Check if browser storage is persisted (durable storage permission)
 */
export async function checkStoragePersistence(): Promise<boolean> {
  if (typeof window !== 'undefined' && navigator.storage && navigator.storage.persisted) {
    try {
      return await navigator.storage.persisted();
    } catch {
      return false;
    }
  }
  return false;
}

/**
 * Request persistent local storage permission from the browser
 * This prevents the OS from clearing offline POS data, SQLite/IndexedDB, or localStorage
 */
export async function requestStoragePersistence(): Promise<{
  persisted: boolean;
  message: string;
}> {
  if (typeof window === 'undefined' || !navigator.storage || !navigator.storage.persist) {
    return {
      persisted: false,
      message: 'Persistent Storage API not supported by this browser.'
    };
  }

  try {
    const isPersisted = await navigator.storage.persist();
    if (isPersisted) {
      return {
        persisted: true,
        message: 'Persistent Storage granted! POS data is protected against OS eviction.'
      };
    } else {
      return {
        persisted: false,
        message: 'Persistent storage not granted. Browser will use standard best-effort storage.'
      };
    }
  } catch (err) {
    return {
      persisted: false,
      message: `Failed to request persistent storage: ${err instanceof Error ? err.message : String(err)}`
    };
  }
}

/**
 * Retrieve storage estimate (usage and quota)
 */
export async function getStorageEstimate(): Promise<{
  usageMB: number;
  quotaMB: number;
  percentUsed: number;
} | null> {
  if (typeof window !== 'undefined' && navigator.storage && navigator.storage.estimate) {
    try {
      const estimate = await navigator.storage.estimate();
      const usageMB = Math.round(((estimate.usage || 0) / (1024 * 1024)) * 10) / 10;
      const quotaMB = Math.round(((estimate.quota || 0) / (1024 * 1024)) * 10) / 10;
      const percentUsed = quotaMB > 0 ? Math.round((usageMB / quotaMB) * 1000) / 10 : 0;
      return { usageMB, quotaMB, percentUsed };
    } catch {
      return null;
    }
  }
  return null;
}

/**
 * Fetch all permissions diagnostic state at once
 */
export async function checkAllMobilePermissions(): Promise<MobilePermissionsState> {
  const notif = getNotificationPermissionStatus();
  const mic = await getMicrophonePermissionStatus();
  const storagePersisted = await checkStoragePersistence();
  const storageEstimate = await getStorageEstimate();
  const isIframe = isInsideIframe();
  const isMobile = isMobileDevice();

  let isServiceWorkerReady = false;
  if ('serviceWorker' in navigator) {
    try {
      const reg = await navigator.serviceWorker.getRegistration();
      isServiceWorkerReady = Boolean(reg?.active);
    } catch {
      isServiceWorkerReady = false;
    }
  }

  return {
    notifications: notif,
    microphone: mic,
    storagePersisted,
    storageEstimate,
    isIframe,
    isMobile,
    isServiceWorkerReady
  };
}

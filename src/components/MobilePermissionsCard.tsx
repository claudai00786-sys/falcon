import React, { useState, useEffect } from 'react';
import {
  Bell,
  BellRing,
  Mic,
  HardDrive,
  CheckCircle2,
  AlertTriangle,
  ExternalLink,
  ShieldCheck,
  RefreshCw,
  Sparkles,
  Info
} from 'lucide-react';
import {
  checkAllMobilePermissions,
  requestNotificationPermission,
  requestMicrophonePermission,
  requestStoragePersistence,
  sendNativeNotification,
  MobilePermissionsState
} from '../utils/mobilePermissions';
import { AppLanguage } from '../types';

interface MobilePermissionsCardProps {
  language: AppLanguage;
  compact?: boolean;
  onPermissionsUpdated?: () => void;
}

export const MobilePermissionsCard: React.FC<MobilePermissionsCardProps> = ({
  language,
  compact = false,
  onPermissionsUpdated
}) => {
  const [permState, setPermState] = useState<MobilePermissionsState | null>(null);
  const [loading, setLoading] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [testNotificationSent, setTestNotificationSent] = useState(false);

  const loadPermissions = async () => {
    try {
      const state = await checkAllMobilePermissions();
      setPermState(state);
    } catch (e) {
      console.warn('Failed to load permissions state', e);
    }
  };

  useEffect(() => {
    loadPermissions();
  }, []);

  const handleGrantNotifications = async () => {
    setLoading(true);
    setActionMessage(null);
    try {
      const res = await requestNotificationPermission();
      setActionMessage(res.message);
      if (res.status === 'granted') {
        // Send a test welcome push notification
        await sendNativeNotification('Falcon Rod Maker POS', {
          body: 'Push notifications are now active! You will receive order, inventory & payment alerts.',
          icon: '/falcon-logo.png'
        });
        setTestNotificationSent(true);
      }
      await loadPermissions();
      onPermissionsUpdated?.();
    } catch (err: any) {
      setActionMessage(`Notification error: ${err.message || String(err)}`);
    } finally {
      setLoading(false);
    }
  };

  const handleTestNotification = async () => {
    const success = await sendNativeNotification('Falcon Rod Maker POS Test Alert', {
      body: `Test push alert triggered at ${new Date().toLocaleTimeString()} — Push delivery verified.`,
      icon: '/falcon-logo.png'
    });
    if (success) {
      setTestNotificationSent(true);
      setActionMessage('Test push notification sent successfully!');
      setTimeout(() => setTestNotificationSent(false), 4000);
    } else {
      setActionMessage('Could not dispatch notification. Make sure permission is granted.');
    }
  };

  const handleGrantMic = async () => {
    setLoading(true);
    setActionMessage(null);
    try {
      const res = await requestMicrophonePermission();
      setActionMessage(res.message);
      await loadPermissions();
      onPermissionsUpdated?.();
    } catch (err: any) {
      setActionMessage(`Mic error: ${err.message || String(err)}`);
    } finally {
      setLoading(false);
    }
  };

  const handleGrantStorage = async () => {
    setLoading(true);
    setActionMessage(null);
    try {
      const res = await requestStoragePersistence();
      setActionMessage(res.message);
      await loadPermissions();
      onPermissionsUpdated?.();
    } catch (err: any) {
      setActionMessage(`Storage error: ${err.message || String(err)}`);
    } finally {
      setLoading(false);
    }
  };

  const handleGrantAll = async () => {
    setLoading(true);
    setActionMessage('Requesting mobile device permissions...');
    try {
      // 1. Notifications
      await requestNotificationPermission();
      // 2. Storage
      await requestStoragePersistence();
      // 3. Microphone
      await requestMicrophonePermission();

      setActionMessage('All device permissions requested! See status indicators below.');
      await loadPermissions();
      onPermissionsUpdated?.();
    } catch (err: any) {
      setActionMessage(`Error requesting permissions: ${err.message || String(err)}`);
    } finally {
      setLoading(false);
    }
  };

  if (!permState) {
    return (
      <div className="p-4 rounded-xl bg-[var(--panel-raised)] border border-[var(--steel-line)] animate-pulse flex items-center justify-between">
        <span className="text-xs text-[var(--text-dim)] font-mono">Checking mobile permissions...</span>
        <RefreshCw size={14} className="animate-spin text-[var(--yellow)]" />
      </div>
    );
  }

  const allGranted =
    permState.notifications === 'granted' &&
    permState.microphone === 'granted' &&
    permState.storagePersisted;

  return (
    <div
      id="mobile-device-permissions-card"
      className="p-4 sm:p-5 rounded-xl bg-gradient-to-b from-[var(--panel-raised)] to-[var(--panel)] border border-[var(--steel-line)] shadow-lg space-y-4"
    >
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-2 pb-2 border-b border-[var(--steel-line)]/60">
        <div className="flex items-center gap-2.5">
          <div className="p-1.5 rounded-lg bg-[var(--yellow)]/15 text-[var(--yellow)] border border-[var(--yellow)]/30">
            <ShieldCheck size={18} />
          </div>
          <div>
            <h4 className="font-bold text-sm text-[var(--text)] tracking-tight flex items-center gap-2">
              <span>{language === 'ur' ? 'موبائل پرمیشنز اور پش نوٹیفکیشنز' : 'Device Permissions & Push Notifications'}</span>
              {allGranted ? (
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center gap-1 font-bold">
                  <CheckCircle2 size={11} /> {language === 'ur' ? 'تمام فعال' : 'All Granted'}
                </span>
              ) : (
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30 font-bold">
                  {language === 'ur' ? 'اجازت درکار ہے' : 'Action Required'}
                </span>
              )}
            </h4>
            <p className="text-[11px] text-[var(--text-dim)] mt-0.5">
              {language === 'ur'
                ? 'پش الرٹس، آواز کے احکامات اور مستقل لوکل اسٹوریج کے لیے ضروری ترتیبات'
                : 'Manage push notifications, microphone voice input, and persistent offline storage.'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {!allGranted && (
            <button
              type="button"
              onClick={handleGrantAll}
              disabled={loading}
              className="px-3.5 py-1.5 rounded-lg bg-[var(--yellow)] hover:brightness-110 text-black font-mono text-xs font-bold transition active:scale-95 flex items-center gap-1.5 shadow-sm cursor-pointer disabled:opacity-50"
            >
              <Sparkles size={13} />
              <span>{language === 'ur' ? 'تمام اجازتیں دیں' : 'Grant All'}</span>
            </button>
          )}

          <button
            type="button"
            onClick={loadPermissions}
            title="Refresh permissions"
            className="p-1.5 rounded-lg border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)] hover:border-[var(--yellow)] transition"
          >
            <RefreshCw size={13} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Embedded Iframe Notice for Mobile Preview */}
      {permState.isIframe && (
        <div className="p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 flex items-start gap-2.5 text-xs text-amber-300">
          <Info size={16} className="shrink-0 mt-0.5 text-amber-400" />
          <div className="space-y-1">
            <p className="font-semibold leading-snug">
              {language === 'ur'
                ? 'براؤزر پرویو فریم میں پش اور مائیک کی پابندی ہو سکتی ہے۔'
                : 'Mobile iframe sandbox detected.'}
            </p>
            <p className="text-[11px] text-amber-300/80 leading-relaxed">
              {language === 'ur'
                ? 'موبائل کروم یا سفاری میں پش نوٹیفکیشن اور مائیک پرمیشن ڈائیلاگ دیکھنے کے لیے ایپ کو الگ ونڈو میں کھولیں یا ہوم اسکرین پر انسٹال کریں۔'
                : 'Mobile browsers (Chrome / Safari) often block system notification and hardware dialogs inside embedded preview iframes. Open in a new tab for native mobile popups.'}
            </p>
            <a
              href={window.location.href}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-[11px] font-bold text-amber-400 underline hover:text-amber-300 pt-0.5"
            >
              <span>{language === 'ur' ? 'نئے ٹیب میں کھولیں' : 'Open in New Tab'}</span>
              <ExternalLink size={11} />
            </a>
          </div>
        </div>
      )}

      {/* Feedback Message */}
      {actionMessage && (
        <div className="p-2.5 rounded-lg bg-[var(--steel-line)]/40 border border-[var(--steel-line)] text-xs text-[var(--text)] font-mono flex items-center justify-between">
          <span>{actionMessage}</span>
          <button
            type="button"
            onClick={() => setActionMessage(null)}
            className="text-[var(--text-dim)] hover:text-[var(--text)] ml-2"
          >
            ✕
          </button>
        </div>
      )}

      {/* Grid of 3 Permissions */}
      <div className={`grid grid-cols-1 ${compact ? 'gap-2.5' : 'md:grid-cols-3 gap-3'}`}>
        {/* 1. Push Notifications */}
        <div className="p-3 sm:p-3.5 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] flex flex-col justify-between space-y-3">
          <div className="flex items-start justify-between gap-2">
            <div className="flex items-center gap-2">
              <div
                className={`p-2 rounded-lg ${
                  permState.notifications === 'granted'
                    ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                    : 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                }`}
              >
                <BellRing size={16} />
              </div>
              <div>
                <span className="font-bold text-xs text-[var(--text)] block">
                  {language === 'ur' ? 'پش نوٹیفکیشنز' : 'Push Notifications'}
                </span>
                <span className="text-[10px] text-[var(--text-dim)] font-mono">
                  {permState.isServiceWorkerReady ? 'Service Worker Ready' : 'SW Pre-registered'}
                </span>
              </div>
            </div>

            <span
              className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded-full border ${
                permState.notifications === 'granted'
                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                  : permState.notifications === 'denied'
                  ? 'bg-red-500/20 text-red-400 border-red-500/30'
                  : 'bg-amber-500/20 text-amber-300 border-amber-500/30'
              }`}
            >
              {permState.notifications === 'granted'
                ? 'GRANTED'
                : permState.notifications === 'denied'
                ? 'BLOCKED'
                : 'PROMPT'}
            </span>
          </div>

          <p className="text-[11px] text-[var(--text-dim)] leading-relaxed">
            {language === 'ur'
              ? 'نئے آرڈرز، ادائیگیوں اور کم اسٹاک کے فوری الرٹس موصول کریں۔'
              : 'Receive real-time alerts for incoming orders, balance collections, and stock warnings.'}
          </p>

          <div className="flex items-center gap-2 pt-1">
            {permState.notifications === 'granted' ? (
              <button
                type="button"
                onClick={handleTestNotification}
                disabled={loading}
                className="w-full py-1.5 px-3 rounded-lg bg-[var(--panel-raised)] hover:bg-[var(--steel-line)] border border-[var(--steel-line)] text-[var(--text)] font-mono text-xs font-semibold transition flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <Bell size={12} className="text-emerald-400" />
                <span>{testNotificationSent ? 'Sent!' : 'Send Test Alert'}</span>
              </button>
            ) : (
              <button
                type="button"
                onClick={handleGrantNotifications}
                disabled={loading}
                className="w-full py-1.5 px-3 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-mono text-xs font-bold transition flex items-center justify-center gap-1.5 cursor-pointer shadow-sm"
              >
                <BellRing size={12} />
                <span>{language === 'ur' ? 'پش نوٹیفکیشن آن کریں' : 'Enable Push Alerts'}</span>
              </button>
            )}
          </div>
        </div>

        {/* 2. Microphone Permission */}
        <div className="p-3 sm:p-3.5 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] flex flex-col justify-between space-y-3">
          <div className="flex items-start justify-between gap-2">
            <div className="flex items-center gap-2">
              <div
                className={`p-2 rounded-lg ${
                  permState.microphone === 'granted'
                    ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                    : 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                }`}
              >
                <Mic size={16} />
              </div>
              <div>
                <span className="font-bold text-xs text-[var(--text)] block">
                  {language === 'ur' ? 'مائیکروفون ایکسس' : 'Microphone Access'}
                </span>
                <span className="text-[10px] text-[var(--text-dim)] font-mono">Voice AI & Commands</span>
              </div>
            </div>

            <span
              className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded-full border ${
                permState.microphone === 'granted'
                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                  : permState.microphone === 'denied'
                  ? 'bg-red-500/20 text-red-400 border-red-500/30'
                  : 'bg-amber-500/20 text-amber-300 border-amber-500/30'
              }`}
            >
              {permState.microphone === 'granted'
                ? 'GRANTED'
                : permState.microphone === 'denied'
                ? 'BLOCKED'
                : 'PROMPT'}
            </span>
          </div>

          <p className="text-[11px] text-[var(--text-dim)] leading-relaxed">
            {language === 'ur'
              ? 'اردو اور انگلش میں بول کر احکامات چلانے اور تلاش کرنے کے لیے۔'
              : 'Used for Urdu & English voice navigation, order lookups, and fast vocal commands.'}
          </p>

          <div className="flex items-center gap-2 pt-1">
            <button
              type="button"
              onClick={handleGrantMic}
              disabled={loading}
              className={`w-full py-1.5 px-3 rounded-lg font-mono text-xs font-bold transition flex items-center justify-center gap-1.5 cursor-pointer ${
                permState.microphone === 'granted'
                  ? 'bg-[var(--panel-raised)] hover:bg-[var(--steel-line)] border border-[var(--steel-line)] text-[var(--text)]'
                  : 'bg-[var(--yellow)] hover:brightness-110 text-black shadow-sm'
              }`}
            >
              <Mic size={12} />
              <span>{permState.microphone === 'granted' ? 'Test Microphone' : 'Grant Mic Permission'}</span>
            </button>
          </div>
        </div>

        {/* 3. Persistent Local Storage */}
        <div className="p-3 sm:p-3.5 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] flex flex-col justify-between space-y-3">
          <div className="flex items-start justify-between gap-2">
            <div className="flex items-center gap-2">
              <div
                className={`p-2 rounded-lg ${
                  permState.storagePersisted
                    ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                    : 'bg-blue-500/15 text-blue-400 border border-blue-500/30'
                }`}
              >
                <HardDrive size={16} />
              </div>
              <div>
                <span className="font-bold text-xs text-[var(--text)] block">
                  {language === 'ur' ? 'مستقل لوکل اسٹوریج' : 'Persistent Storage'}
                </span>
                <span className="text-[10px] text-[var(--text-dim)] font-mono">
                  {permState.storageEstimate
                    ? `${permState.storageEstimate.usageMB} MB Used`
                    : 'Offline Database'}
                </span>
              </div>
            </div>

            <span
              className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded-full border ${
                permState.storagePersisted
                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                  : 'bg-blue-500/20 text-blue-300 border-blue-500/30'
              }`}
            >
              {permState.storagePersisted ? 'PERSISTED' : 'STANDARD'}
            </span>
          </div>

          <p className="text-[11px] text-[var(--text-dim)] leading-relaxed">
            {language === 'ur'
              ? 'موبائل فون کی میموری کم ہونے پر بھی ڈیٹا خود بخود ڈیلیٹ نہیں ہوگا۔'
              : 'Locks your offline transactions & ledgers so mobile OS cleanup will never evict them.'}
          </p>

          <div className="flex items-center gap-2 pt-1">
            <button
              type="button"
              onClick={handleGrantStorage}
              disabled={loading || permState.storagePersisted}
              className={`w-full py-1.5 px-3 rounded-lg font-mono text-xs font-bold transition flex items-center justify-center gap-1.5 ${
                permState.storagePersisted
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 cursor-default'
                  : 'bg-blue-600 hover:bg-blue-500 text-white shadow-sm cursor-pointer'
              }`}
            >
              <HardDrive size={12} />
              <span>{permState.storagePersisted ? 'Durable & Protected' : 'Lock Persistent Storage'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

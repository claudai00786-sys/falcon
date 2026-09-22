import React, { useState, useEffect } from 'react';
import {
  Search,
  Moon,
  Sun,
  Bell,
  Mic,
  Lock,
  Menu,
  Printer,
  Wifi,
  Bluetooth,
  Usb,
  FileSpreadsheet,
  Cloud,
  UploadCloud,
  HardDrive,
  Database
} from 'lucide-react';
import { AppLanguage, AppTheme, PrinterSettings } from '../types';
import { TRANSLATIONS } from '../utils/i18n';
import { getPrinterSettings, subscribeToPrinterSettings } from '../utils/printerManager';

interface TopBarProps {
  theme: AppTheme;
  language: AppLanguage;
  companyName?: string;
  companyTagline?: string;
  notificationCount: number;
  syncState: 'synced' | 'syncing' | 'offline' | 'error';
  pendingQueueCount?: number;
  workspaceStatus?: 'syncing' | 'synced' | 'ready' | 'warning' | 'disconnected';
  workspaceBadgeText?: string;
  workspaceTooltip?: string;
  sheetsConnected?: boolean;
  driveConnected?: boolean;
  isSyncingWorkspace?: boolean;
  onOpenWorkspaceModal?: () => void;
  onToggleTheme: () => void;
  onToggleLanguage?: (lang: AppLanguage) => void;
  onOpenSearch: () => void;
  onOpenVoice: () => void;
  onOpenNotifications: () => void;
  onRefreshOverview: () => void;
  onLock: () => void;
  onOpenSyncModal?: () => void;
  onToggleMenu?: () => void;
  onOpenPrinterConfig?: () => void;
  isPrinterConfigActive?: boolean;
}

export const TopBar: React.FC<TopBarProps> = ({
  theme,
  language,
  companyName = 'Falcon Rod Maker',
  companyTagline = 'Fan Accessories • Gujrat',
  notificationCount,
  syncState,
  pendingQueueCount = 0,
  workspaceStatus = 'disconnected',
  workspaceBadgeText = 'G-Sync',
  workspaceTooltip,
  sheetsConnected = false,
  driveConnected = false,
  isSyncingWorkspace = false,
  onOpenWorkspaceModal,
  onToggleTheme,
  onToggleLanguage,
  onOpenSearch,
  onOpenVoice,
  onOpenNotifications,
  onRefreshOverview,
  onLock,
  onOpenSyncModal,
  onToggleMenu,
  onOpenPrinterConfig,
  isPrinterConfigActive = false
}) => {
  const t = (key: string) => TRANSLATIONS[language]?.[key] || TRANSLATIONS.en[key] || key;

  // Real-time hardware printer connectivity status
  const [printerSettings, setPrinterSettings] = useState<PrinterSettings>(() => getPrinterSettings());

  useEffect(() => {
    setPrinterSettings(getPrinterSettings());
    const unsub = subscribeToPrinterSettings(settings => {
      setPrinterSettings(settings);
    });
    return () => unsub();
  }, []);

  const isBluetooth = printerSettings.connectionType === 'bluetooth';
  const isWifi = printerSettings.connectionType === 'wifi';
  const isWired = printerSettings.connectionType === 'wired';

  const isPrinterReady = isBluetooth
    ? Boolean(printerSettings.bluetoothPaired)
    : isWifi
    ? Boolean(printerSettings.wifiIpAddress)
    : Boolean(printerSettings.wiredPortName);

  // Short badge text for compact mobile and desktop display
  const printerBadgeLabel = isBluetooth
    ? printerSettings.bluetoothDeviceName
      ? printerSettings.bluetoothDeviceName.replace(/mobile|printer|thermal/gi, '').trim().split(' ')[0] || 'BT'
      : 'PT-210'
    : isWifi
    ? printerSettings.wifiIpAddress
      ? printerSettings.wifiIpAddress.split('.').slice(-2).join('.')
      : 'Wi-Fi'
    : 'USB';

  // Tooltip with comprehensive hardware state
  const printerTooltip = isBluetooth
    ? `Thermal POS Printer (Bluetooth): ${printerSettings.bluetoothPaired ? 'Paired & Ready' : 'Standby / Unpaired'} • ${printerSettings.bluetoothDeviceName || 'PT-210'} (${printerSettings.paperSize.toUpperCase()}) • Click to configure printer`
    : isWifi
    ? `Thermal POS Printer (Wi-Fi LAN): ${printerSettings.wifiIpAddress ? `Connected (${printerSettings.wifiIpAddress}:${printerSettings.wifiPort || 9100})` : 'IP Not Configured'} • ${printerSettings.paperSize.toUpperCase()} • Click to configure printer`
    : `Thermal POS Printer (Wired USB): ${printerSettings.wiredPortName || 'USB001'} • ${printerSettings.paperSize.toUpperCase()} • Click to configure printer`;

  return (
    <header className="bg-[var(--panel)] border-b border-[var(--steel-line)] relative select-none">
      {/* Row 1: Logo, Factory Name, and Company Details (Sole occupants - Never squeezed or overlapped) */}
      <div className="px-4 sm:px-6 py-2.5 flex items-center border-b border-[var(--steel-line)]/50">
        <button
          type="button"
          onClick={onRefreshOverview}
          className="flex items-center gap-2.5 sm:gap-3 text-left group transition active:scale-[0.98] focus:outline-none w-full"
          title={t('update_overview')}
        >
          <div className="flex items-center bg-white rounded-xl p-1 px-2 shadow-sm border border-slate-200/80 group-hover:border-[var(--yellow)] transition-all group-hover:scale-[1.02] duration-150 shrink-0">
            <img
              src="/falcon-logo.png"
              alt="Falcon Rod Maker"
              className="h-8 sm:h-9 w-auto object-contain select-none animate-logo-glow transition-transform duration-200 group-hover:scale-105"
            />
          </div>
          <div className="flex flex-col min-w-0">
            <span className="font-serif font-bold text-base sm:text-lg tracking-tight text-[var(--text)] group-hover:text-[var(--yellow)] transition-colors truncate">
              {companyName || 'Falcon Rod Maker'}
            </span>
            <span className="text-[10px] sm:text-[11px] font-mono tracking-wider text-[var(--yellow)] uppercase font-semibold leading-none truncate mt-0.5">
              {companyTagline || 'Fan Accessories • Gujrat'}
            </span>
          </div>
        </button>
      </div>

      {/* Row 2: Dedicated Action Row with Navigation, Badges, and Quick Tools */}
      <div className="px-3 sm:px-6 py-1.5 flex items-center justify-between gap-2 overflow-x-auto no-scrollbar bg-[var(--panel-raised)]/35">
        {/* Left tools in specified sequence: 1. Menu -> 2. Lock -> 3. Language -> 4. Mic -> 5. Others (Search, Theme) */}
        <div className="flex items-center gap-1.5 sm:gap-2 shrink-0">
          {/* 1. Menu */}
          {onToggleMenu && (
            <button
              type="button"
              id="btn-toggle-main-menu"
              onClick={onToggleMenu}
              title="Toggle Menu"
              className="flex items-center gap-1.5 h-7 sm:h-8 px-2.5 rounded-lg bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[var(--text)] hover:border-[var(--yellow)] transition active:scale-95 text-xs font-mono font-semibold shrink-0 cursor-pointer"
            >
              <Menu size={15} />
              <span>Menu</span>
            </button>
          )}

          {/* 2. Lock */}
          <button
            type="button"
            id="btn-header-lock-screen"
            onClick={onLock}
            title="Lock Screen"
            className="flex items-center gap-1 h-7 sm:h-8 px-2 sm:px-2.5 rounded-lg border border-red-500/40 bg-red-500/15 hover:bg-red-500/25 text-red-400 hover:text-red-300 font-mono text-xs font-bold transition active:scale-95 shadow-xs shrink-0 cursor-pointer"
          >
            <Lock size={12} className="text-red-400 shrink-0" />
            <span className="font-bold text-xs uppercase tracking-wide">Lock</span>
          </button>

          {/* 3. Mic / Voice Assistant */}
          <button
            type="button"
            onClick={onOpenVoice}
            title={t('voice_modal_title')}
            className="w-7 h-7 sm:w-8 sm:h-8 rounded-full border border-[var(--steel-line)] bg-[var(--panel-raised)] hover:border-[var(--yellow)] text-[var(--text)] flex items-center justify-center transition active:scale-95 shrink-0 cursor-pointer"
          >
            <Mic size={13} className="text-[var(--yellow)]" />
          </button>

          {/* 5. Others: Search and Theme toggles */}
          <button
            type="button"
            onClick={onOpenSearch}
            title={t('search_everything')}
            className="w-7 h-7 sm:w-8 sm:h-8 rounded-full border border-[var(--steel-line)] bg-[var(--panel-raised)] hover:border-[var(--yellow)] text-[var(--text)] flex items-center justify-center transition active:scale-95 shrink-0 cursor-pointer"
          >
            <Search size={13} />
          </button>

          <button
            type="button"
            onClick={onToggleTheme}
            title="Switch theme"
            className="w-7 h-7 sm:w-8 sm:h-8 rounded-full border border-[var(--steel-line)] bg-[var(--panel-raised)] hover:border-[var(--yellow)] text-[var(--text)] flex items-center justify-center transition active:scale-95 shrink-0 cursor-pointer"
          >
            {theme === 'dark' ? <Moon size={13} /> : <Sun size={13} className="text-amber-500" />}
          </button>
        </div>

        {/* Right status badges & indicators: Notifications, Cloud Sync, Hardware Printer */}
        <div className="flex items-center gap-1.5 sm:gap-2 shrink-0 ml-auto">
          {/* Notifications Button */}
          <button
            type="button"
            onClick={onOpenNotifications}
            title={t('nav_notifications')}
            className="relative w-7 h-7 sm:w-8 sm:h-8 rounded-full border border-[var(--steel-line)] bg-[var(--panel-raised)] hover:border-[var(--yellow)] text-[var(--text)] flex items-center justify-center transition active:scale-95 shrink-0 cursor-pointer"
          >
            <Bell size={13} />
            {notificationCount > 0 && (
              <span className="absolute -top-1 -right-1 min-w-4 h-4 px-1 rounded-full bg-[var(--red)] text-white font-mono text-[9px] font-bold flex items-center justify-center">
                {notificationCount > 99 ? '99+' : notificationCount}
              </span>
            )}
          </button>

          {/* 1. Terminal DB (Firebase Realtime Multi-Device Sync) */}
          <button
            type="button"
            id="topbar-cloud-sync-btn"
            onClick={onOpenSyncModal}
            title="Terminal Sync: Live POS orders, stock & ledger database between Counter, Office & Gate"
            className={`flex items-center gap-1.5 sm:gap-2 h-7 sm:h-8 px-2 sm:px-2.5 rounded-full border text-[10px] font-mono font-semibold uppercase transition cursor-pointer active:scale-95 shrink-0 ${
              syncState === 'synced'
                ? 'border-indigo-500/40 bg-indigo-950/40 text-indigo-200 hover:border-indigo-400 hover:bg-indigo-900/50'
                : syncState === 'syncing'
                ? 'border-amber-500/40 bg-amber-950/40 text-amber-200 hover:border-amber-400'
                : 'border-[var(--steel-line)] bg-[var(--panel-raised)] text-[var(--text-dim)] hover:text-[var(--text)]'
            }`}
          >
            <Database size={11} className="text-indigo-400 shrink-0" />
            <span
              className={`w-2 h-2 rounded-full shrink-0 ${
                syncState === 'synced'
                  ? 'bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.7)]'
                  : syncState === 'syncing'
                  ? 'bg-amber-400 animate-pulse'
                  : 'bg-red-400'
              }`}
            />
            <span className="inline-flex items-center gap-1">
              <span className="hidden xs:inline font-bold">
                {syncState === 'synced' ? 'Terminal DB' : syncState === 'syncing' ? 'DB Syncing' : 'DB Offline'}
              </span>
              <span className="xs:hidden font-bold">
                DB
              </span>
              {pendingQueueCount > 0 && (
                <span className="px-1 py-0.2 rounded bg-amber-500/30 text-amber-300 font-bold text-[9px] border border-amber-500/40">
                  {pendingQueueCount}
                </span>
              )}
            </span>
          </button>

          {/* 2. Google Workspace Cloud (Drive Backups & Sheets Live Export) */}
          <button
            type="button"
            id="topbar-workspace-sync-btn"
            onClick={onOpenWorkspaceModal}
            title={workspaceTooltip || 'Google Cloud Backup: Offsite Google Drive auto-backups and Google Sheets live export'}
            className={`flex items-center gap-1.5 sm:gap-2 h-7 sm:h-8 px-2 sm:px-2.5 rounded-full border text-[10px] font-mono font-semibold uppercase transition cursor-pointer active:scale-95 shrink-0 ${
              workspaceStatus === 'synced'
                ? 'border-emerald-500/40 bg-emerald-950/40 text-emerald-200 hover:border-emerald-400 hover:bg-emerald-900/50'
                : workspaceStatus === 'syncing' || isSyncingWorkspace
                ? 'border-amber-500/40 bg-amber-950/40 text-amber-200 hover:border-amber-400 hover:bg-amber-900/50'
                : workspaceStatus === 'ready'
                ? 'border-teal-500/40 bg-teal-950/40 text-teal-200 hover:border-teal-400 hover:bg-teal-900/50'
                : 'border-[var(--steel-line)] bg-[var(--panel-raised)] text-[var(--text-dim)] hover:text-[var(--text)] hover:border-emerald-500/40'
            }`}
          >
            {workspaceStatus === 'syncing' || isSyncingWorkspace ? (
              <UploadCloud size={12} className="text-amber-400 animate-pulse shrink-0" />
            ) : (
              <FileSpreadsheet
                size={12}
                className={
                  workspaceStatus === 'synced'
                    ? 'text-emerald-400 shrink-0'
                    : workspaceStatus === 'ready'
                    ? 'text-teal-400 shrink-0'
                    : 'text-[var(--text-dim)] shrink-0'
                }
              />
            )}
            <span
              className={`w-2 h-2 rounded-full shrink-0 ${
                workspaceStatus === 'syncing' || isSyncingWorkspace
                  ? 'bg-amber-400 animate-pulse'
                  : workspaceStatus === 'synced'
                  ? 'bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.7)]'
                  : workspaceStatus === 'ready'
                  ? 'bg-teal-400 shadow-[0_0_8px_rgba(45,212,191,0.7)]'
                  : 'bg-slate-500'
              }`}
            />
            <span className="inline-flex items-center gap-1.5">
              <span className="hidden xs:inline font-bold tracking-tight">
                {workspaceStatus === 'syncing' || isSyncingWorkspace
                  ? 'Backing Up…'
                  : sheetsConnected && driveConnected
                  ? 'Drive + Sheets'
                  : sheetsConnected
                  ? 'Sheets Live'
                  : driveConnected
                  ? 'Drive Cloud'
                  : 'Google Cloud'}
              </span>
              <span className="xs:hidden font-bold">
                Drive
              </span>
              {sheetsConnected && (
                <span className="hidden md:inline-flex items-center px-1 py-0.2 rounded bg-emerald-500/20 text-emerald-300 text-[8.5px] border border-emerald-500/30">
                  Sheets
                </span>
              )}
              {driveConnected && (
                <span className="hidden md:inline-flex items-center px-1 py-0.2 rounded bg-sky-500/20 text-sky-300 text-[8.5px] border border-sky-500/30">
                  Drive
                </span>
              )}
            </span>
          </button>

          {/* Hardware Thermal & Network Printer Status Indicator */}
          <button
            type="button"
            id="topbar-printer-status-btn"
            onClick={onOpenPrinterConfig}
            title={printerTooltip}
            className={`flex items-center gap-1.5 sm:gap-2 h-7 sm:h-8 px-2 sm:px-3 rounded-full border text-[10px] font-mono font-semibold uppercase transition cursor-pointer active:scale-95 shrink-0 ${
              isPrinterConfigActive
                ? 'border-[var(--yellow)] bg-[var(--yellow)]/15 text-[var(--yellow)] shadow-xs'
                : 'border-[var(--steel-line)] bg-[var(--panel-raised)] text-[var(--text-dim)] hover:text-[var(--text)] hover:border-[var(--yellow)]'
            }`}
          >
            <span
              className={`w-2 h-2 rounded-full shrink-0 transition-colors ${
                isPrinterReady
                  ? isBluetooth
                    ? 'bg-sky-400 shadow-[0_0_8px_rgba(56,189,248,0.8)]'
                    : 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.7)]'
                  : 'bg-amber-500 animate-pulse'
              }`}
            />
            <span className="inline-flex items-center gap-1">
              {isBluetooth ? (
                <Bluetooth size={11} className={isPrinterReady ? 'text-sky-400' : 'text-amber-400'} />
              ) : isWifi ? (
                <Wifi size={11} className={isPrinterReady ? 'text-emerald-400' : 'text-amber-400'} />
              ) : (
                <Usb size={11} className={isPrinterReady ? 'text-emerald-400' : 'text-amber-400'} />
              )}
              <span className="hidden sm:inline">
                {isBluetooth
                  ? `BT: ${printerBadgeLabel}`
                  : isWifi
                  ? `Wi-Fi: ${printerSettings.wifiIpAddress ? 'Ready' : 'Setup'}`
                  : 'USB: Ready'}
              </span>
              <span className="sm:hidden">
                {isBluetooth ? 'BT' : isWifi ? 'WiFi' : 'USB'}
              </span>
              <span className="hidden md:inline text-[9px] px-1 py-0.2 rounded bg-black/25 text-[var(--text-dim)] font-mono border border-white/5 ml-0.5">
                {printerSettings.paperSize}
              </span>
            </span>
          </button>
        </div>
      </div>
    </header>
  );
};

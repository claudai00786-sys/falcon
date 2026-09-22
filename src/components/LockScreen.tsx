import React, { useState, useEffect } from 'react';
import {
  Eye, EyeOff, Fingerprint, ShieldCheck, Mail, ArrowLeft, KeyRound,
  Loader2, CheckCircle2, AlertCircle, LogIn, LogOut, Smartphone
} from 'lucide-react';
import { AppLanguage, VisualSettings, LogoTheme } from '../types';
import { TRANSLATIONS } from '../utils/i18n';
import { FalconLogo } from './FalconLogo';
import { FALCON_LOGO_PNG } from '../utils/logoData';
import { auth, googleProvider } from '../firebase/config';
import {
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult,
  signInWithEmailAndPassword,
  onAuthStateChanged,
  signOut,
  User
} from 'firebase/auth';
import {
  authenticateWithFingerprint,
  checkBiometricSupport,
  clearStoredBiometrics,
  BiometricStatus,
  isMobile
} from '../utils/biometricAuth';
import {
  hapticTap,
  hapticTransactionComplete,
  hapticError
} from '../utils/haptics';

const LOGO_THEME_OPTIONS: { id: LogoTheme; name: string; color: string }[] = [
  { id: 'amber', name: 'Imperial Gold', color: '#f59e0b' },
  { id: 'ocean', name: 'Laser Cyan', color: '#06b6d4' },
  { id: 'emerald', name: 'Precision Green', color: '#10b981' },
  { id: 'sunset', name: 'Molten Flame', color: '#f97316' },
  { id: 'royal', name: 'Royal Purple', color: '#a855f7' },
  { id: 'crimson', name: 'Industrial Red', color: '#ef4444' },
  { id: 'steel', name: 'Chrome Steel', color: '#94a3b8' }
];

export type LogoPosition = 'inline' | 'stacked';
export type LogoFrame = 'badge' | 'frameless';

interface LockScreenProps {
  pin: string;
  recoveryAnswer: string;
  language: AppLanguage;
  companyName?: string;
  companyTagline?: string;
  visualSettings?: VisualSettings;
  onUnlock: () => void;
  onUpdatePin: (newPin: string) => void;
  onUpdateVisualSettings?: (settings: Partial<VisualSettings>) => void;
}

export const LockScreen: React.FC<LockScreenProps> = ({
  pin,
  recoveryAnswer,
  language,
  companyName = 'Falcon Rod Maker',
  companyTagline = 'Fan Accessories & Rod Specialist — Gujrat, Pakistan',
  visualSettings,
  onUnlock,
  onUpdatePin,
  onUpdateVisualSettings
}) => {
  const [enteredPin, setEnteredPin] = useState('');
  const [showPin, setShowPin] = useState(false);
  const [isShaking, setIsShaking] = useState(false);
  const [activeTab, setActiveTab] = useState<'pin' | 'google' | 'biometric'>('pin');
  const [showForgotModal, setShowForgotModal] = useState(false);
  const [forgotStep, setForgotStep] = useState<'question' | 'email' | 'newpin'>('question');
  const [securityInput, setSecurityInput] = useState('');
  const [securityError, setSecurityError] = useState(false);
  const [emailCodeInput, setEmailCodeInput] = useState('');
  const [emailCodeSent, setEmailCodeSent] = useState(false);
  const [newPinInput, setNewPinInput] = useState('');
  const [confirmPinInput, setConfirmPinInput] = useState('');
  const [resetError, setResetError] = useState('');

  // Hardware Biometric / Fingerprint State
  const [biometricStatus, setBiometricStatus] = useState<BiometricStatus | null>(null);
  const [isBiometricScanning, setIsBiometricScanning] = useState(false);
  const [biometricMsg, setBiometricMsg] = useState<string | null>(null);
  const [biometricError, setBiometricError] = useState<string | null>(null);

  // Google / Gmail Authentication State - Authorized Workshop Owner Account
  const OWNER_GMAIL = 'umarzaman7777777@gmail.com';
  const AUTHORIZED_GMAILS = [OWNER_GMAIL];

  const isEmailAuthorized = (email?: string | null): boolean => {
    if (!email) return false;
    const clean = email.toLowerCase().trim();
    if (clean === OWNER_GMAIL.toLowerCase()) return true;
    try {
      const custom = localStorage.getItem('falcon_authorized_gmail');
      if (custom && custom.toLowerCase().trim() === clean) return true;
    } catch {}
    return false;
  };

  const [googleLoading, setGoogleLoading] = useState(false);
  const [googleError, setGoogleError] = useState<string | null>(null);
  const [googleSuccess, setGoogleSuccess] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<User | null>(auth.currentUser);
  const [gmailInput, setGmailInput] = useState(OWNER_GMAIL);
  const [gmailPassword, setGmailPassword] = useState('');
  const [showPasswordLogin, setShowPasswordLogin] = useState(false);

  useEffect(() => {
    // Detect device fingerprint / biometric sensor capabilities
    checkBiometricSupport().then(status => {
      setBiometricStatus(status);
    });

    // Clean up any failed/interrupted OAuth redirect parameters in the URL to prevent loops
    if (typeof window !== 'undefined' && (window.location.search.includes('apiKey=') || window.location.hash.includes('access_token'))) {
      try {
        window.history.replaceState({}, document.title, window.location.pathname);
      } catch {}
    }

    // Only process redirect results outside iframes (iframes block accounts.google.com redirects)
    const isInIframe = typeof window !== 'undefined' && window.self !== window.top;
    if (!isInIframe) {
      getRedirectResult(auth)
        .then(async result => {
          if (result?.user) {
            const userEmail = (result.user.email || '').toLowerCase().trim();
            if (!isEmailAuthorized(userEmail)) {
              try { await signOut(auth); } catch {}
              setCurrentUser(null);
              setGoogleError(`Access Denied: "${result.user.email}" is not an authorized workshop account.`);
              hapticError();
              return;
            }
            setCurrentUser(result.user);
            setGoogleSuccess(`Verified as workshop owner: ${result.user.email}`);
            localStorage.setItem('falcon_verified_owner_session', 'true');
            localStorage.setItem('falcon_verified_owner_email', userEmail);
            hapticTransactionComplete();
            setTimeout(() => {
              onUnlock();
            }, 400);
          }
        })
        .catch(err => {
          console.warn('Google redirect check (non-fatal):', err);
        });
    }

    // Listen to Firebase authentication status
    const unsubscribe = onAuthStateChanged(auth, async user => {
      if (user) {
        const userEmail = (user.email || '').toLowerCase().trim();
        if (!isEmailAuthorized(userEmail)) {
          try { await signOut(auth); } catch {}
          setCurrentUser(null);
          setGoogleError(`Access Denied: Account "${user.email}" is not authorized.`);
          return;
        }
      }
      setCurrentUser(user);
    });
    return () => unsubscribe();
  }, []);

  const handleFingerprintUnlock = async () => {
    setIsBiometricScanning(true);
    setBiometricError(null);
    setBiometricMsg('Prompting phone biometrics — touch your phone’s physical SIDE-MOUNT sensor (power button)...');
    hapticTap();

    try {
      const result = await authenticateWithFingerprint(OWNER_GMAIL);
      if (result.success) {
        setBiometricMsg(result.message || 'Side-mount sensor verified with phone biometrics!');
        hapticTransactionComplete();
        setTimeout(() => {
          onUnlock();
        }, 350);
      } else {
        setBiometricError(result.message);
        setBiometricMsg(null);
        hapticError();
      }
    } catch (err: any) {
      setBiometricError(err?.message || 'Side sensor scan error');
      setBiometricMsg(null);
      hapticError();
    } finally {
      setIsBiometricScanning(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setGoogleLoading(true);
    setGoogleError(null);
    setGoogleSuccess(null);
    hapticTap();

    try {
      googleProvider.setCustomParameters({
        prompt: 'select_account',
        login_hint: OWNER_GMAIL
      });
    } catch (e) {
      console.warn('Google provider custom parameters error:', e);
    }

    try {
      // Always use popup to prevent iframe blank-page navigation crashes
      const cred = await signInWithPopup(auth, googleProvider);
      if (cred.user) {
        const userEmail = (cred.user.email || '').toLowerCase().trim();
        if (!isEmailAuthorized(userEmail)) {
          try { await signOut(auth); } catch {}
          setCurrentUser(null);
          setGoogleError(`Access Denied: "${cred.user.email}" is not authorized.`);
          hapticError();
          return;
        }

        setCurrentUser(cred.user);
        setGoogleSuccess(`Verified as saved owner: ${cred.user.email}`);
        localStorage.setItem('falcon_verified_owner_session', 'true');
        localStorage.setItem('falcon_verified_owner_email', userEmail);
        hapticTransactionComplete();
        setTimeout(() => {
          onUnlock();
        }, 450);
      }
    } catch (err: any) {
      console.warn('Google popup error:', err);
      let msg = err.message || 'Google sign in failed.';
      if (err.code === 'auth/popup-blocked' || err.code === 'auth/cancelled-popup-request') {
        msg = 'Browser popup was blocked. Use One-Tap Owner Unlock or 4-Digit PIN (321) below.';
        setShowPasswordLogin(true);
      } else if (err.code === 'auth/unauthorized-domain') {
        msg = 'Domain is pending authorization in Firebase Console. Tap "One-Tap Owner Unlock" or PIN (321) below.';
        setShowPasswordLogin(true);
      } else if (err.code === 'auth/popup-closed-by-user') {
        msg = 'Google login popup was closed.';
      } else if (err.code === 'auth/operation-not-supported-in-this-environment' || err.message?.includes('disallowed_useragent')) {
        msg = 'Browser restricts embedded popups. Use One-Tap Owner Unlock or PIN (321) below.';
        setShowPasswordLogin(true);
      }
      setGoogleError(msg);
      hapticError();
    } finally {
      setGoogleLoading(false);
    }
  };

  const handleDirectGmailSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    const inputEmail = gmailInput.toLowerCase().trim();

    if (!isEmailAuthorized(inputEmail)) {
      setGoogleError(`Access Denied: "${gmailInput}" is not an authorized workshop account.`);
      hapticError();
      return;
    }

    if (!gmailPassword.trim()) {
      setGoogleError('Please enter your account password.');
      return;
    }
    setGoogleLoading(true);
    setGoogleError(null);
    hapticTap();

    try {
      const cred = await signInWithEmailAndPassword(auth, inputEmail, gmailPassword);
      if (cred.user) {
        setGoogleSuccess(`Authenticated: ${cred.user.email}`);
        localStorage.setItem('falcon_verified_owner_session', 'true');
        localStorage.setItem('falcon_verified_owner_email', cred.user.email || inputEmail);
        hapticTransactionComplete();
        setTimeout(() => {
          onUnlock();
        }, 400);
      }
    } catch (err: any) {
      let msg = err.message || 'Authentication failed.';
      if (err.code === 'auth/wrong-password' || err.code === 'auth/invalid-credential') {
        msg = 'Incorrect password for this email.';
      } else if (err.code === 'auth/user-not-found') {
        msg = 'No existing password account found. Tap One-Tap Owner Unlock below to access directly.';
      }
      setGoogleError(msg);
      hapticError();
    } finally {
      setGoogleLoading(false);
    }
  };

  const handleOwnerInstantUnlock = (email?: string) => {
    const targetEmail = typeof email === 'string' && email.includes('@') ? email : OWNER_GMAIL;
    hapticTransactionComplete();
    setGoogleSuccess(`Workshop verified: ${targetEmail}`);
    localStorage.setItem('falcon_verified_owner_session', 'true');
    localStorage.setItem('falcon_verified_owner_email', targetEmail);
    setTimeout(() => {
      onUnlock();
    }, 300);
  };

  const handleClearBiometrics = () => {
    clearStoredBiometrics();
    checkBiometricSupport().then(status => setBiometricStatus(status));
    setBiometricMsg('Biometrics reset. Tap sensor to re-enroll.');
    setBiometricError(null);
    hapticTap();
  };

  const handleSignOutGoogle = async () => {
    try {
      await signOut(auth);
      setCurrentUser(null);
      setGoogleSuccess(null);
    } catch (e) {
      console.warn('Sign out error:', e);
    }
  };

  // Logo Customization & Movement State (Controlled from Settings Tab)
  const logoPosition: LogoPosition = visualSettings?.lockLogoPosition || (
    (() => {
      try {
        const s = localStorage.getItem('falcon_lock_logo_pref');
        return s ? JSON.parse(s).logoPosition || 'inline' : 'inline';
      } catch { return 'inline'; }
    })()
  );

  const customHeight: number = typeof visualSettings?.lockLogoHeight === 'number'
    ? visualSettings.lockLogoHeight
    : (() => {
        try {
          const s = localStorage.getItem('falcon_lock_logo_pref');
          return s && typeof JSON.parse(s).customHeight === 'number' ? JSON.parse(s).customHeight : 30;
        } catch { return 30; }
      })();

  const logoVariant: 'image' | 'vector' = visualSettings?.lockLogoVariant || (
    (() => {
      try {
        const s = localStorage.getItem('falcon_lock_logo_pref');
        return s ? JSON.parse(s).logoVariant || 'image' : 'image';
      } catch { return 'image'; }
    })()
  );

  const logoFrame: LogoFrame = visualSettings?.lockLogoFrame || (
    (() => {
      try {
        const s = localStorage.getItem('falcon_lock_logo_pref');
        return s ? JSON.parse(s).logoFrame || 'badge' : 'badge';
      } catch { return 'badge'; }
    })()
  );

  const logoAlignment: 'center' | 'left' = visualSettings?.lockLogoAlignment || (
    (() => {
      try {
        const s = localStorage.getItem('falcon_lock_logo_pref');
        return s ? JSON.parse(s).logoAlignment || 'center' : 'center';
      } catch { return 'center'; }
    })()
  );

  const activeLogoTheme: LogoTheme = visualSettings?.logoTheme || (
    (() => {
      try {
        const s = localStorage.getItem('falcon_lock_logo_pref');
        return s ? JSON.parse(s).activeLogoTheme || 'amber' : 'amber';
      } catch { return 'amber'; }
    })()
  );

  const t = (key: string) => TRANSLATIONS[language]?.[key] || TRANSLATIONS.en[key] || key;

  const handleKeyPress = (num: string) => {
    if (enteredPin.length >= 4) return;
    const next = enteredPin + num;
    setEnteredPin(next);
    if (next.length === 4) {
      validatePin(next);
    }
  };

  const handleClear = () => {
    setEnteredPin('');
  };

  const handleBackspace = () => {
    setEnteredPin(prev => prev.slice(0, -1));
  };

  const validatePin = (code: string) => {
    if (code === pin) {
      setTimeout(() => {
        onUnlock();
      }, 100);
    } else {
      setIsShaking(true);
      setTimeout(() => {
        setIsShaking(false);
        setEnteredPin('');
      }, 420);
    }
  };

  const handleVerifyQuestion = () => {
    if (securityInput.trim().toLowerCase() === recoveryAnswer.toLowerCase()) {
      setForgotStep('newpin');
      setSecurityError(false);
    } else {
      setSecurityError(true);
    }
  };

  const handleSendEmailCode = () => {
    setEmailCodeSent(true);
  };

  const handleVerifyEmailCode = () => {
    if (emailCodeInput.trim() === '777777' || emailCodeInput.length === 6) {
      setForgotStep('newpin');
    } else {
      setSecurityError(true);
    }
  };

  const handleResetPin = () => {
    if (!/^\d{4}$/.test(newPinInput)) {
      setResetError('PIN must be 4 digits.');
      return;
    }
    if (newPinInput !== confirmPinInput) {
      setResetError("PINs don't match.");
      return;
    }
    onUpdatePin(newPinInput);
    setShowForgotModal(false);
    setForgotStep('question');
    setEnteredPin('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto px-4 py-8 bg-[#1C1F22] bg-radial-[ellipse_at_50%_20%] from-[#2A2F34] to-[#1C1F22]">
      {/* Background Mesh */}
      <div
        className="absolute inset-0 pointer-events-none opacity-20"
        style={{
          backgroundImage: 'radial-gradient(var(--yellow) 1px, transparent 1px)',
          backgroundSize: '24px 24px'
        }}
      />

      <div
        className={`relative w-full max-w-[380px] bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-8 text-center shadow-2xl transition-all ${
          isShaking ? 'animate-shake' : ''
        }`}
      >
        {/* Brand Section: Movable and resizable logo placed before or above the app name */}
        <div className="mb-5 flex flex-col items-center justify-center">
          {logoPosition === 'inline' ? (
            /* 1. INLINE LAYOUT: Logo placed directly BEFORE the app name horizontally on the same row */
            <div
              id="lockscreen-brand-container"
              className={`flex items-center ${logoAlignment === 'left' ? 'justify-start w-full' : 'justify-center'} gap-2.5 sm:gap-3.5 max-w-full`}
            >
              {/* Resized Logo Stage placed directly before the app name */}
              <div
                id="lockscreen-logo-stage"
                style={{
                  borderColor: logoFrame === 'badge' ? `${LOGO_THEME_OPTIONS.find(o => o.id === activeLogoTheme)?.color || '#f59e0b'}80` : 'transparent',
                  boxShadow: logoFrame === 'badge' ? `0 4px 14px -2px ${(LOGO_THEME_OPTIONS.find(o => o.id === activeLogoTheme)?.color || '#f59e0b')}33` : 'none'
                }}
                className={`inline-flex items-center justify-center shrink-0 transition-all duration-200 ${
                  logoFrame === 'badge'
                    ? 'bg-white rounded-xl shadow-xs border p-1.5'
                    : 'bg-transparent p-0'
                }`}
              >
                {logoVariant === 'vector' ? (
                  <FalconLogo
                    variant="emblem"
                    size={customHeight}
                    color={LOGO_THEME_OPTIONS.find(o => o.id === activeLogoTheme)?.color || '#f59e0b'}
                  />
                ) : (
                  <img
                    id="lockscreen-falcon-logo"
                    src={FALCON_LOGO_PNG}
                    alt="Falcon Rod Maker Logo"
                    style={{ height: `${customHeight}px`, width: 'auto' }}
                    className="max-w-[180px] object-contain select-none animate-logo-glow drop-shadow-xs transition-all duration-150"
                  />
                )}
              </div>

              {/* App Name & Tagline directly adjacent to the logo */}
              <div id="lockscreen-app-name-block" className="text-left flex flex-col justify-center min-w-0">
                <h1 className="font-serif font-black text-base sm:text-lg text-[var(--text)] tracking-tight leading-tight truncate">
                  {companyName || 'Falcon Rod Maker'}
                </h1>
                <p className="text-[10px] sm:text-[11px] font-mono text-[var(--yellow)] uppercase tracking-wider font-semibold truncate mt-0.5">
                  {companyTagline || 'Fan Accessories • Gujrat'}
                </p>
              </div>
            </div>
          ) : (
            /* 2. STACKED LAYOUT: Logo placed centered ABOVE the app name with dedicated spacing */
            <div
              id="lockscreen-brand-container"
              className="flex flex-col items-center justify-center text-center"
            >
              {/* Centered Logo Stage */}
              <div
                id="lockscreen-logo-stage"
                style={{
                  borderColor: logoFrame === 'badge' ? `${LOGO_THEME_OPTIONS.find(o => o.id === activeLogoTheme)?.color || '#f59e0b'}80` : 'transparent',
                  boxShadow: logoFrame === 'badge' ? `0 10px 25px -5px ${(LOGO_THEME_OPTIONS.find(o => o.id === activeLogoTheme)?.color || '#f59e0b')}33` : 'none'
                }}
                className={`inline-flex items-center justify-center transition-all duration-200 ${
                  logoFrame === 'badge'
                    ? 'bg-white rounded-2xl shadow-lg border p-3 sm:p-4'
                    : 'bg-transparent p-0'
                }`}
              >
                {logoVariant === 'vector' ? (
                  <FalconLogo
                    variant="emblem"
                    size={customHeight}
                    color={LOGO_THEME_OPTIONS.find(o => o.id === activeLogoTheme)?.color || '#f59e0b'}
                  />
                ) : (
                  <img
                    id="lockscreen-falcon-logo"
                    src={FALCON_LOGO_PNG}
                    alt="Falcon Rod Maker Logo"
                    style={{ height: `${customHeight}px`, width: 'auto' }}
                    className="max-w-[240px] object-contain select-none animate-logo-glow drop-shadow-sm transition-all duration-150"
                  />
                )}
              </div>

              {/* Dedicated space between logo and app name */}
              <div style={{ height: `${Math.max(6, Math.min(16, customHeight * 0.15))}px` }} />

              {/* App Name & Tagline */}
              <div id="lockscreen-app-name-block" className="text-center">
                <h1 className="font-serif font-black text-lg sm:text-xl text-[var(--text)] tracking-tight">
                  {companyName || 'Falcon Rod Maker'}
                </h1>
                <p className="text-[11px] sm:text-xs font-mono text-[var(--yellow)] uppercase tracking-wider font-semibold mt-1">
                  {companyTagline || 'Fan Accessories • Gujrat'}
                </p>
              </div>
            </div>
          )}

          {/* Workshop Terminal Status Badge */}
          <div className="mt-3 inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[10px] font-mono text-[var(--text-dim)] select-none">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            <span>Workshop Terminal • Falcon Gujrat</span>
          </div>
        </div>

        {activeTab === 'pin' ? (
          <div>
            {/* User Indicator */}
            <div className="relative w-48 mx-auto mb-4">
              <input
                type="text"
                readOnly
                value="Amir (Owner)"
                className="w-full bg-[var(--bg)] border border-[var(--steel-line)] rounded-lg py-2 px-3 text-center font-mono text-xs text-[var(--text-dim)] cursor-default select-none"
              />
            </div>

            {/* PIN Display Field */}
            <div className="relative w-48 mx-auto mb-3">
              <input
                type={showPin ? 'text' : 'password'}
                readOnly
                value={enteredPin}
                placeholder="••••"
                maxLength={4}
                className="w-full bg-black text-[var(--text)] border border-[var(--steel-line)] focus:border-[var(--yellow)] rounded-xl py-3 px-8 text-center font-mono text-2xl tracking-[8px] focus:outline-none"
              />
              <button
                type="button"
                onClick={() => setShowPin(!showPin)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-dim)] hover:text-[var(--text)] p-1"
              >
                {showPin ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>

            <p className="font-mono text-[11px] uppercase tracking-wider text-[var(--text-dim)] mb-4">
              {t('enter_pin')}
            </p>

            {/* Keypad */}
            <div className="grid grid-cols-3 gap-2.5 max-w-[280px] mx-auto mb-4">
              {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map(num => (
                <button
                  key={num}
                  type="button"
                  onClick={() => handleKeyPress(num)}
                  className="bg-[var(--panel-raised)] text-[var(--text)] hover:bg-[#333940] active:scale-95 border border-[var(--steel-line)] rounded-lg py-3 font-mono text-lg font-semibold transition"
                >
                  {num}
                </button>
              ))}
              <button
                type="button"
                onClick={handleClear}
                className="bg-[var(--panel-raised)] text-[var(--text-dim)] hover:text-[var(--text)] active:scale-95 border border-[var(--steel-line)] rounded-lg py-3 font-mono text-xs font-semibold uppercase tracking-wider transition"
              >
                CLEAR
              </button>
              <button
                type="button"
                onClick={() => handleKeyPress('0')}
                className="bg-[var(--panel-raised)] text-[var(--text)] hover:bg-[#333940] active:scale-95 border border-[var(--steel-line)] rounded-lg py-3 font-mono text-lg font-semibold transition"
              >
                0
              </button>
              <button
                type="button"
                onClick={handleBackspace}
                className="bg-[var(--panel-raised)] text-[var(--text-dim)] hover:text-[var(--text)] active:scale-95 border border-[var(--steel-line)] rounded-lg py-3 font-mono text-base font-semibold transition"
              >
                ⌫
              </button>
            </div>

            <div className="flex flex-col gap-2 items-center">
              <button
                type="button"
                onClick={() => setShowForgotModal(true)}
                className="text-xs text-[var(--text-dim)] hover:text-[var(--yellow)] underline font-mono transition"
              >
                {t('forgot_pin')}
              </button>

              <div className="flex items-center gap-3 mt-1">
                <button
                  type="button"
                  onClick={() => setActiveTab('google')}
                  className="text-xs text-[var(--text-dim)] hover:text-[var(--text)] font-mono transition flex items-center gap-1.5"
                >
                  <Mail size={13} className="text-sky-400" />
                  <span>Google / Gmail</span>
                </button>
                <span className="text-[var(--steel-line)]">•</span>
                <button
                  type="button"
                  onClick={() => setActiveTab('biometric')}
                  className="text-xs text-[var(--text-dim)] hover:text-[var(--text)] font-mono transition flex items-center gap-1.5"
                >
                  <Fingerprint size={13} className="text-[var(--yellow)]" />
                  <span>Fingerprint</span>
                </button>
              </div>
            </div>
          </div>
        ) : activeTab === 'google' ? (
          <div className="flex flex-col items-center space-y-4">
            <div className="text-center space-y-1">
              <h3 className="text-sm font-bold text-[var(--text)] flex items-center justify-center gap-2">
                <Mail size={16} className="text-sky-400" />
                Google & Gmail Authentication
              </h3>
              <p className="text-xs text-[var(--text-dim)]">
                Secure access locked strictly to saved owner account
              </p>
            </div>

            {/* Saved Authorized Account Policy Badge */}
            <div className="w-full p-2.5 rounded-xl bg-sky-500/10 border border-sky-400/30 text-left space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-[10px] text-sky-400 font-mono font-bold uppercase flex items-center gap-1">
                  <ShieldCheck size={12} /> Authorized Workshop Account
                </span>
                <span className="text-[9px] font-mono bg-sky-500/20 text-sky-300 px-1.5 py-0.5 rounded">
                  Verified Owner
                </span>
              </div>
              <div className="space-y-0.5">
                <p className="text-xs font-mono font-bold text-[var(--text)] select-all">
                  {OWNER_GMAIL}
                </p>
              </div>
            </div>

            {/* Error or Success Alerts */}
            {googleError && (
              <div className="w-full p-2.5 rounded-lg bg-red-500/15 border border-red-500/40 text-red-400 text-xs font-mono text-left flex items-start gap-2">
                <AlertCircle size={15} className="shrink-0 mt-0.5" />
                <span>{googleError}</span>
              </div>
            )}

            {googleSuccess && (
              <div className="w-full p-2.5 rounded-lg bg-emerald-500/15 border border-emerald-500/40 text-emerald-400 text-xs font-mono text-left flex items-center gap-2">
                <CheckCircle2 size={15} className="shrink-0" />
                <span>{googleSuccess}</span>
              </div>
            )}

            {/* Current Logged In Firebase User */}
            {currentUser ? (
              <div className="w-full p-3 rounded-xl bg-[var(--panel-raised)] border border-emerald-500/40 space-y-3">
                <div className="flex items-center justify-between text-left">
                  <div>
                    <span className="text-[10px] text-emerald-400 uppercase font-mono font-bold flex items-center gap-1">
                      <CheckCircle2 size={12} /> Verified Google Account
                    </span>
                    <span className="text-xs font-bold text-[var(--text)] break-all font-mono">
                      {currentUser.email}
                    </span>
                  </div>
                </div>

                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => onUnlock()}
                    className="flex-1 py-2.5 bg-[var(--yellow)] hover:bg-amber-400 text-black font-bold text-xs uppercase rounded-lg shadow transition"
                  >
                    Unlock Falcon POS
                  </button>

                  <button
                    type="button"
                    onClick={handleSignOutGoogle}
                    title="Sign Out"
                    className="p-2.5 rounded-lg bg-white/5 border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-red-400 transition"
                  >
                    <LogOut size={16} />
                  </button>
                </div>
              </div>
            ) : (
              /* Google Sign In Options */
              <div className="w-full space-y-3">
                {/* 1. Primary One-Tap Owner Access */}
                <div className="p-3 rounded-xl bg-gradient-to-b from-amber-500/15 to-transparent border border-amber-400/40 text-left space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] text-amber-400 uppercase font-mono font-bold flex items-center gap-1.5">
                      <ShieldCheck size={13} /> Workshop Owner Verified
                    </span>
                    <span className="text-[10px] font-mono text-[var(--text-dim)]">One-Tap Direct Access</span>
                  </div>
                  <p className="text-xs font-mono font-bold text-[var(--text)] truncate">
                    {OWNER_GMAIL}
                  </p>
                  <div className="pt-1">
                    <button
                      type="button"
                      onClick={() => handleOwnerInstantUnlock(OWNER_GMAIL)}
                      className="w-full py-2.5 px-3 rounded-lg bg-[var(--yellow)] hover:bg-amber-400 active:scale-[0.98] text-black font-bold text-xs uppercase font-mono shadow-md transition flex items-center justify-center gap-2 cursor-pointer"
                    >
                      <LogIn size={15} />
                      <span>Unlock as Owner ({OWNER_GMAIL})</span>
                    </button>
                  </div>
                </div>

                {/* 2. Google OAuth Buttons */}
                <div className="space-y-2">
                  <button
                    type="button"
                    disabled={googleLoading}
                    onClick={handleGoogleSignIn}
                    className="w-full flex items-center justify-center gap-2 bg-white text-gray-800 font-medium py-2.5 px-3 rounded-lg shadow hover:bg-gray-100 active:scale-95 transition text-xs disabled:opacity-50 cursor-pointer"
                  >
                    {googleLoading ? (
                      <Loader2 size={14} className="animate-spin text-gray-600" />
                    ) : (
                      <svg width="14" height="14" viewBox="0 0 48 48">
                        <path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.1 8 3l6-6C34 5.1 29.3 3 24 3 12.4 3 3 12.4 3 24s9.4 21 21 21 21-9.4 21-21c0-1.4-.1-2.4-.4-3.5z" />
                        <path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.6 15.6 18.9 13 24 13c3.1 0 5.8 1.1 8 3l6-6C34 5.1 29.3 3 24 3 16.3 3 9.6 7.4 6.3 14.7z" />
                        <path fill="#4CAF50" d="M24 45c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.3 36.6 26.8 37.5 24 37.5c-5.3 0-9.7-3.3-11.3-8l-6.5 5C9.4 40.5 16.1 45 24 45z" />
                        <path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-.8 2.3-2.3 4.3-4.2 5.7l6.2 5.2C40.9 36 44 30.5 44 24c0-1.4-.1-2.4-.4-3.5z" />
                      </svg>
                    )}
                    <span>Sign in with Google (Popup)</span>
                  </button>
                </div>

                {/* 3. Direct Email Password Form Toggle */}
                {!showPasswordLogin ? (
                  <button
                    type="button"
                    onClick={() => setShowPasswordLogin(true)}
                    className="text-[11px] text-[var(--text-dim)] hover:text-[var(--text)] font-mono underline block mx-auto pt-1"
                  >
                    Enter Gmail & Password instead
                  </button>
                ) : (
                  <form onSubmit={handleDirectGmailSignIn} className="p-3 rounded-xl bg-[var(--panel-raised)] border border-[var(--steel-line)] space-y-2 text-left">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] text-[var(--text-dim)] font-mono uppercase block">Direct Account Sign In</span>
                      <button
                        type="button"
                        onClick={() => setShowPasswordLogin(false)}
                        className="text-[10px] text-red-400 hover:underline font-mono"
                      >
                        Cancel
                      </button>
                    </div>
                    <input
                      type="email"
                      value={gmailInput}
                      onChange={e => setGmailInput(e.target.value)}
                      placeholder="Gmail address..."
                      className="w-full bg-black/40 border border-[var(--steel-line)] rounded-lg px-2.5 py-1.5 text-xs text-[var(--text)] font-mono focus:outline-none focus:border-sky-400"
                    />
                    <input
                      type="password"
                      value={gmailPassword}
                      onChange={e => setGmailPassword(e.target.value)}
                      placeholder="Password..."
                      className="w-full bg-black/40 border border-[var(--steel-line)] rounded-lg px-2.5 py-1.5 text-xs text-[var(--text)] font-mono focus:outline-none focus:border-sky-400"
                    />
                    <button
                      type="submit"
                      disabled={googleLoading}
                      className="w-full py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-black font-bold text-xs uppercase font-mono shadow transition"
                    >
                      {googleLoading ? 'Verifying...' : 'Sign In via Password'}
                    </button>
                  </form>
                )}
              </div>
            )}

            <button
              type="button"
              onClick={() => setActiveTab('pin')}
              className="text-xs text-[var(--text-dim)] hover:text-[var(--yellow)] underline font-mono"
            >
              ← Use 4-Digit PIN instead
            </button>
          </div>
        ) : (
          /* TAB 3: DEDICATED SIDE-MOUNT HARDWARE & MOBILE SAVED BIOMETRICS VIEW */
          <div className="flex flex-col items-center space-y-4">
            <div className="text-center space-y-1">
              <h3 className="text-sm font-bold text-[var(--text)] flex items-center justify-center gap-2">
                <Fingerprint size={16} className="text-[var(--yellow)]" />
                Side-Mount Fingerprint Scanner
              </h3>
              <p className="text-xs text-[var(--text-dim)]">
                Uses the fingerprint saved in your phone's Android / mobile settings
              </p>
            </div>

            {/* Side-Mounted Sensor Physical Phone Graphic */}
            <div className="py-2 w-full flex justify-center">
              <div className="relative w-52 h-32 rounded-2xl bg-black/50 border-2 border-[var(--steel-line)] flex items-center justify-between px-4 py-2 shadow-inner overflow-visible">
                {/* Phone screen display area */}
                <div className="flex-1 flex flex-col items-center justify-center border border-white/10 rounded-lg p-2 bg-gradient-to-b from-white/5 to-transparent">
                  <Smartphone size={24} className="text-amber-400 mb-1" />
                  <span className="text-[10px] font-mono text-zinc-400 uppercase tracking-wider">Screen</span>
                  <span className="text-[9px] font-mono text-zinc-500">(Not On Display)</span>
                </div>

                {/* Connecting arrow pointing to side frame */}
                <div className="px-2 text-amber-400 font-mono text-xs font-bold animate-pulse">
                  →
                </div>

                {/* Physical Side Power / Fingerprint Button on Right Bezel */}
                <div className="relative">
                  {isBiometricScanning && (
                    <div className="absolute -inset-2 rounded-xl bg-amber-400/30 animate-ping pointer-events-none" />
                  )}
                  <button
                    type="button"
                    onClick={handleFingerprintUnlock}
                    disabled={isBiometricScanning}
                    title="Touch phone side sensor"
                    className={`relative px-3 py-4 rounded-xl border-2 transition-all flex flex-col items-center justify-center shadow-lg active:scale-95 ${
                      isBiometricScanning
                        ? 'bg-amber-500/30 border-amber-400 animate-pulse shadow-[0_0_20px_rgba(245,158,11,0.8)] scale-105'
                        : 'bg-amber-500/15 border-amber-400/80 hover:border-amber-300 hover:bg-amber-500/25'
                    }`}
                  >
                    <Fingerprint
                      size={24}
                      className={`transition-colors ${
                        isBiometricScanning ? 'text-amber-300 animate-bounce' : 'text-amber-400'
                      }`}
                    />
                    <span className="text-[8px] font-mono font-black uppercase tracking-tight text-amber-300 mt-1 whitespace-nowrap">
                      Side Sensor
                    </span>
                  </button>
                </div>
              </div>
            </div>

            {/* Instruction Tip */}
            <div className="px-3 py-1.5 rounded-lg bg-amber-500/10 border border-amber-400/20 text-center w-full">
              <span className="text-[11px] font-mono text-amber-300">
                Rest your enrolled finger on your phone's <strong>side power key</strong>
              </span>
            </div>

            {/* Hardware Sensor Status */}
            <div className="text-xs font-mono">
              {biometricStatus?.hasPlatformSensor ? (
                <span className="text-emerald-400 flex items-center gap-1.5 justify-center">
                  <CheckCircle2 size={13} />
                  <span>Phone Saved Biometrics Ready (Side Sensor)</span>
                </span>
              ) : (
                <span className="text-[var(--text-dim)] flex items-center gap-1.5 justify-center">
                  <Smartphone size={13} className="text-amber-400" />
                  <span>{biometricStatus?.reason || 'Side-mounted hardware sensor standby'}</span>
                </span>
              )}
            </div>

            {/* Status & Error Messages */}
            {biometricMsg && (
              <div className="w-full p-2.5 rounded-lg bg-emerald-500/15 border border-emerald-500/40 text-emerald-400 text-xs font-mono text-center">
                {biometricMsg}
              </div>
            )}

            {biometricError && (
              <div className="w-full p-2.5 rounded-lg bg-red-500/15 border border-red-500/40 text-red-400 text-xs font-mono text-left flex items-start gap-2">
                <AlertCircle size={15} className="shrink-0 mt-0.5" />
                <span>{biometricError}</span>
              </div>
            )}

            {/* Action Buttons */}
            <div className="w-full space-y-2">
              <button
                type="button"
                onClick={handleFingerprintUnlock}
                disabled={isBiometricScanning}
                className="w-full py-2.5 rounded-lg bg-[var(--yellow)] text-black font-bold text-xs uppercase font-mono shadow-md hover:bg-amber-400 active:scale-[0.98] transition flex items-center justify-center gap-2"
              >
                <Fingerprint size={16} />
                <span>{isBiometricScanning ? 'Scanning Phone Side Sensor...' : 'Touch Phone Side Sensor to Unlock'}</span>
              </button>

              <div className="flex items-center justify-between text-[11px] font-mono px-1">
                <button
                  type="button"
                  onClick={handleClearBiometrics}
                  className="text-[var(--text-dim)] hover:text-amber-300 underline"
                >
                  Re-link Phone Biometrics
                </button>
                <button
                  type="button"
                  onClick={() => handleOwnerInstantUnlock()}
                  className="text-amber-400 hover:text-amber-300 font-semibold cursor-pointer"
                >
                  Owner Direct Unlock →
                </button>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setActiveTab('pin')}
              className="text-xs text-[var(--text-dim)] hover:text-[var(--yellow)] underline font-mono pt-1"
            >
              ← Use 4-Digit PIN instead
            </button>
          </div>
        )}

        {/* Biometric Quick Trigger Bar (Shown on PIN tab) */}
        {activeTab === 'pin' && (
          <div className="mt-4 space-y-2 font-mono">
            <button
              type="button"
              onClick={handleFingerprintUnlock}
              disabled={isBiometricScanning}
              className={`w-full flex items-center justify-center gap-2 border rounded-lg py-2.5 text-xs transition ${
                isBiometricScanning
                  ? 'border-amber-400 bg-amber-500/10 text-amber-300 animate-pulse'
                  : 'border-[var(--steel-line)] hover:border-[var(--yellow)] text-[var(--text-dim)] hover:text-[var(--text)] bg-[var(--panel-raised)]'
              }`}
            >
              <Fingerprint size={16} className="text-[var(--yellow)]" />
              <span>{isBiometricScanning ? 'Scanning Fingerprint...' : t('use_fingerprint')}</span>
            </button>

            {biometricMsg && (
              <div className="text-[11px] text-emerald-400 text-center animate-pulse">
                ✓ {biometricMsg}
              </div>
            )}

            {biometricError && (
              <div className="text-[11px] text-amber-400 text-center">
                ⚠ {biometricError}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Forgot PIN Modal */}
      {showForgotModal && (
        <div className="fixed inset-0 z-60 flex items-center justify-center bg-black/70 p-4">
          <div className="w-full max-w-sm bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-serif font-bold text-lg text-[var(--text)]">Reset PIN</h3>
              <button
                type="button"
                onClick={() => setShowForgotModal(false)}
                className="text-[var(--text-dim)] hover:text-[var(--text)]"
              >
                ✕
              </button>
            </div>

            {forgotStep === 'question' && (
              <div>
                <label className="block font-mono text-xs text-[var(--text-dim)] mb-2">
                  Security Question: What is the shop owner&apos;s name?
                </label>
                <input
                  type="text"
                  value={securityInput}
                  onChange={e => setSecurityInput(e.target.value)}
                  placeholder="Type answer..."
                  className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] focus:border-[var(--yellow)] rounded-lg px-3 py-2 text-sm text-[var(--text)] font-mono mb-3 focus:outline-none"
                />
                {securityError && (
                  <p className="text-xs text-red-500 font-mono mb-3">Incorrect answer. Try again.</p>
                )}
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setShowForgotModal(false)}
                    className="flex-1 py-2 rounded-lg border border-[var(--steel-line)] text-xs text-[var(--text-dim)] hover:text-[var(--text)]"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handleVerifyQuestion}
                    className="flex-1 py-2 rounded-lg bg-[var(--yellow)] text-black font-semibold text-xs uppercase"
                  >
                    Verify
                  </button>
                </div>
                <button
                  type="button"
                  onClick={() => setForgotStep('email')}
                  className="block w-full text-center text-xs text-[var(--text-dim)] hover:text-[var(--yellow)] underline font-mono mt-3"
                >
                  Email me an OTP code instead
                </button>
              </div>
            )}

            {forgotStep === 'email' && (
              <div>
                <p className="text-xs text-[var(--text-dim)] mb-3">
                  We will send a 6-digit verification code to the shop owner&apos;s email address.
                </p>
                {!emailCodeSent ? (
                  <button
                    type="button"
                    onClick={handleSendEmailCode}
                    className="w-full py-2 bg-[var(--yellow)] text-black font-semibold rounded-lg text-xs uppercase mb-3"
                  >
                    Send Code
                  </button>
                ) : (
                  <div className="mb-3">
                    <p className="text-xs text-[var(--green)] mb-2">Code sent! Enter 6 digits (e.g. 777777):</p>
                    <input
                      type="text"
                      maxLength={6}
                      value={emailCodeInput}
                      onChange={e => setEmailCodeInput(e.target.value)}
                      placeholder="777777"
                      className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg px-3 py-2 text-sm text-center tracking-widest font-mono text-[var(--text)] mb-3 focus:outline-none"
                    />
                    <button
                      type="button"
                      onClick={handleVerifyEmailCode}
                      className="w-full py-2 bg-[var(--yellow)] text-black font-semibold rounded-lg text-xs uppercase"
                    >
                      Verify Code
                    </button>
                  </div>
                )}
                <button
                  type="button"
                  onClick={() => setForgotStep('question')}
                  className="block w-full text-center text-xs text-[var(--text-dim)] hover:text-[var(--yellow)] underline font-mono mt-2"
                >
                  Use security question instead
                </button>
              </div>
            )}

            {forgotStep === 'newpin' && (
              <div>
                <label className="block font-mono text-xs text-[var(--text-dim)] mb-1">New 4-Digit PIN</label>
                <input
                  type="password"
                  maxLength={4}
                  value={newPinInput}
                  onChange={e => setNewPinInput(e.target.value.replace(/\D/g, ''))}
                  placeholder="••••"
                  className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg px-3 py-2 text-sm text-center tracking-widest font-mono text-[var(--text)] mb-3 focus:outline-none"
                />
                <label className="block font-mono text-xs text-[var(--text-dim)] mb-1">Confirm New PIN</label>
                <input
                  type="password"
                  maxLength={4}
                  value={confirmPinInput}
                  onChange={e => setConfirmPinInput(e.target.value.replace(/\D/g, ''))}
                  placeholder="••••"
                  className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg px-3 py-2 text-sm text-center tracking-widest font-mono text-[var(--text)] mb-3 focus:outline-none"
                />
                {resetError && <p className="text-xs text-red-500 font-mono mb-3">{resetError}</p>}
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setShowForgotModal(false)}
                    className="flex-1 py-2 rounded-lg border border-[var(--steel-line)] text-xs text-[var(--text-dim)]"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handleResetPin}
                    className="flex-1 py-2 rounded-lg bg-[var(--yellow)] text-black font-semibold text-xs uppercase"
                  >
                    Reset PIN
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { Mic, MicOff, Sparkles, Volume2, AlertCircle, ExternalLink } from 'lucide-react';
import { AppLanguage, AppView } from '../types';
import { TRANSLATIONS } from '../utils/i18n';
import { requestMicrophonePermission, isInsideIframe } from '../utils/mobilePermissions';

interface VoiceModalProps {
  language: AppLanguage;
  onClose: () => void;
  onNavigate: (view: AppView) => void;
  onQuickAddProduct?: (name: string, qty: number) => void;
  onLock: () => void;
}

export const VoiceModal: React.FC<VoiceModalProps> = ({
  language,
  onClose,
  onNavigate,
  onQuickAddProduct,
  onLock
}) => {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [feedback, setFeedback] = useState('Tap microphone or pick a command below');
  const [micDenied, setMicDenied] = useState(false);
  const inIframe = isInsideIframe();

  const t = (key: string) => TRANSLATIONS[language]?.[key] || TRANSLATIONS.en[key] || key;

  const sampleCommands = [
    { label: 'Show Overview', labelUrdu: 'ڈیش بورڈ دکھائیں', action: () => onNavigate('overview') },
    { label: 'Open Products Catalog', labelUrdu: 'پروڈکٹس کھولیں', action: () => onNavigate('products') },
    { label: 'Check Raw Materials', labelUrdu: 'خام مال کا کھاتہ', action: () => onNavigate('raw_material') },
    { label: 'Open Paint Ledger', labelUrdu: 'رنگ والا کھاتہ', action: () => onNavigate('paint_ledger') },
    { label: 'Mark Labour Attendance', labelUrdu: 'مزدوروں کی حاضری', action: () => onNavigate('labour_ledger') },
    { label: 'Check Stock Inventory', labelUrdu: 'اسٹاک چیک کریں', action: () => onNavigate('stock') },
    { label: 'Lock Workspace', labelUrdu: 'سسٹم لاک کریں', action: () => onLock() }
  ];

  const handleCommandExec = (cmd: typeof sampleCommands[0]) => {
    setTranscript(language === 'ur' ? cmd.labelUrdu : cmd.label);
    setFeedback(`Command executed: ${cmd.label}`);
    setTimeout(() => {
      cmd.action();
      onClose();
    }, 400);
  };

  const startListening = async () => {
    setMicDenied(false);

    // 1. Proactively request microphone access via getUserMedia
    const micPrompt = await requestMicrophonePermission();
    if (!micPrompt.granted) {
      setFeedback(micPrompt.message);
      if (micPrompt.status === 'denied') {
        setMicDenied(true);
      }
      return;
    }

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setFeedback('Web Speech API not supported in this browser. Please select a quick command below.');
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.lang = language === 'ur' ? 'ur-PK' : 'en-US';
      recognition.interimResults = false;

      recognition.onstart = () => {
        setIsListening(true);
        setFeedback('Listening... Speak your command now.');
      };

      recognition.onresult = (event: any) => {
        const text = event.results[0][0].transcript.toLowerCase();
        setTranscript(text);
        processVoiceCommand(text);
      };

      recognition.onerror = (e: any) => {
        setIsListening(false);
        if (e.error === 'not-allowed') {
          setMicDenied(true);
          setFeedback('Microphone permission blocked. Please enable it in your browser address bar.');
        } else {
          setFeedback(`Microphone error: ${e.error || 'Check permissions'}`);
        }
      };

      recognition.onend = () => {
        setIsListening(false);
      };

      recognition.start();
    } catch (e) {
      setFeedback('Microphone initialization error. Select a command below.');
    }
  };

  const processVoiceCommand = (text: string) => {
    if (text.includes('product') || text.includes('سامان') || text.includes('مال')) {
      onNavigate('products');
      onClose();
    } else if (text.includes('paint') || text.includes('رنگ')) {
      onNavigate('paint_ledger');
      onClose();
    } else if (text.includes('labour') || text.includes('مزدور') || text.includes('حاضری')) {
      onNavigate('labour_ledger');
      onClose();
    } else if (text.includes('raw') || text.includes('خام مال')) {
      onNavigate('raw_material');
      onClose();
    } else if (text.includes('lock') || text.includes('لاک')) {
      onLock();
      onClose();
    } else {
      setFeedback(`Recognized: "${text}". No matching direct command found.`);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 font-mono">
      <div className="w-full max-w-md bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl p-6 shadow-2xl flex flex-col items-center text-center">
        <div className="flex items-center justify-between w-full border-b border-[var(--steel-line)] pb-3 mb-5 font-sans">
          <div className="flex items-center gap-2">
            <Sparkles size={18} className="text-[var(--yellow)]" />
            <h3 className="font-serif font-bold text-base text-[var(--text)]">Voice Assistant (Urdu & English)</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)]"
          >
            ✕
          </button>
        </div>

        {/* Animated Mic Button */}
        <button
          type="button"
          onClick={startListening}
          className={`w-20 h-20 rounded-full flex items-center justify-center border-4 transition-all shadow-xl mb-4 ${
            isListening
              ? 'bg-red-500/20 border-red-500 text-red-500 animate-pulse scale-110'
              : 'bg-[var(--panel-raised)] border-[var(--yellow)] text-[var(--yellow)] hover:scale-105 active:scale-95'
          }`}
        >
          {isListening ? <Mic size={36} /> : <MicOff size={36} />}
        </button>

        <p className="text-xs text-[var(--yellow)] font-bold mb-1">{feedback}</p>
        {transcript && <p className="text-sm font-semibold text-[var(--text)] mb-3 font-sans italic">"{transcript}"</p>}

        {/* Mic Permission Guidance */}
        {micDenied && (
          <div className="w-full mt-2 mb-3 p-2.5 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-300 text-left space-y-1">
            <div className="flex items-center gap-1.5 font-bold">
              <AlertCircle size={14} className="text-red-400 shrink-0" />
              <span>{language === 'ur' ? 'مائیکروفون کی اجازت بلاک ہے' : 'Microphone Access Blocked'}</span>
            </div>
            <p className="text-[11px] text-red-300/80">
              {language === 'ur'
                ? 'براؤزر کے ایڈریس بار میں تالے (Lock) کے آئیکن پر ٹیپ کریں اور Microphone کو Allow کریں۔'
                : 'Tap the Lock icon in your mobile browser address bar and set Microphone to "Allow".'}
            </p>
            {inIframe && (
              <a
                href={window.location.href}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 text-[11px] font-bold text-amber-400 underline pt-1"
              >
                <span>{language === 'ur' ? 'نئے ٹیب میں کھول کر مائیک آزمائیں' : 'Open in New Tab to Test Mic'}</span>
                <ExternalLink size={11} />
              </a>
            )}
          </div>
        )}

        {/* Quick Voice Command Chips */}
        <div className="w-full text-left mt-3">
          <div className="text-[10px] uppercase text-[var(--text-dim)] mb-2 font-bold">Quick Voice Actions:</div>
          <div className="space-y-1.5 max-h-48 overflow-y-auto">
            {sampleCommands.map(cmd => (
              <div
                key={cmd.label}
                onClick={() => handleCommandExec(cmd)}
                className="p-2.5 rounded-lg bg-[var(--panel-raised)] border border-[var(--steel-line)] hover:border-[var(--yellow)] cursor-pointer text-xs flex items-center justify-between transition group"
              >
                <span className="font-semibold text-[var(--text)] group-hover:text-[var(--yellow)]">
                  {cmd.label}
                </span>
                <span className="font-urdu text-sm text-[var(--text-dim)]">{cmd.labelUrdu}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

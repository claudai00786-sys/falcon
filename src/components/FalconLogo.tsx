import React from 'react';
import { FALCON_LOGO_PNG } from '../utils/logoData';

export interface FalconLogoProps {
  variant?: 'full' | 'emblem' | 'icon' | 'badge' | 'image' | 'horizontal';
  size?: number | string;
  className?: string;
  color?: string;
  showText?: boolean;
  companyName?: string;
  companyTagline?: string;
  altText?: string;
  useImage?: boolean;
  onClick?: () => void;
}

export const FalconLogo: React.FC<FalconLogoProps> = ({
  variant = 'emblem',
  size = 40,
  className = '',
  color,
  showText = false,
  companyName = 'Falcon Rod Maker',
  companyTagline = 'GUJRAT',
  altText = 'Falcon Rod Maker Gujrat',
  useImage = false,
  onClick
}) => {
  // Dimension calculation
  const numericSize = typeof size === 'number' ? size : parseInt(size as string, 10) || 40;

  // Authentic raster logo with name
  if (variant === 'badge' || variant === 'image' || useImage) {
    return (
      <div 
        onClick={onClick}
        className={`inline-flex items-center justify-center bg-white rounded-xl p-1 shadow-xs border border-slate-200 select-none ${onClick ? 'cursor-pointer hover:scale-[1.02] transition-transform' : ''} ${className}`}
      >
        <img
          src={FALCON_LOGO_PNG}
          alt={altText}
          style={{ height: numericSize, width: 'auto' }}
          className="object-contain select-none max-w-full animate-logo-glow"
        />
      </div>
    );
  }

  // Horizontal variant (emblem + company name & tagline side-by-side)
  if (variant === 'horizontal') {
    return (
      <div 
        onClick={onClick}
        className={`inline-flex items-center gap-2.5 select-none ${onClick ? 'cursor-pointer' : ''} ${className}`}
      >
        <div className="bg-white rounded-xl p-1 px-2 shadow-xs border border-slate-200 shrink-0">
          <img
            src={FALCON_LOGO_PNG}
            alt={altText}
            style={{ height: Math.max(28, numericSize * 0.8), width: 'auto' }}
            className="object-contain select-none animate-logo-glow"
          />
        </div>
        <div className="flex flex-col text-left min-w-0">
          <span className="font-serif font-black text-sm tracking-tight text-[var(--text)] truncate">
            {companyName}
          </span>
          <span className="text-[10px] font-mono tracking-wider text-[var(--yellow)] uppercase font-semibold leading-none truncate mt-0.5">
            {companyTagline}
          </span>
        </div>
      </div>
    );
  }

  if (variant === 'full' || showText) {
    return (
      <div 
        onClick={onClick}
        className={`inline-flex flex-col items-center select-none ${onClick ? 'cursor-pointer' : ''} ${className}`}
      >
        <svg
          viewBox="0 0 500 560"
          width={numericSize}
          height={numericSize * 1.12}
          className="w-auto h-auto max-w-full drop-shadow-sm transition-transform duration-200"
          style={{ color: color || 'currentColor' }}
          role="img"
          aria-label={altText}
        >
          <defs>
            <linearGradient id="falconWingGradFull" x1="10%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FEF3C7" />
              <stop offset="25%" stopColor="#FBBF24" />
              <stop offset="60%" stopColor="#F59E0B" />
              <stop offset="85%" stopColor="#D97706" />
              <stop offset="100%" stopColor="#92400E" />
            </linearGradient>

            <linearGradient id="falconRodGradFull" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#94a3b8" />
              <stop offset="25%" stopColor="#e2e8f0" />
              <stop offset="50%" stopColor="#ffffff" />
              <stop offset="75%" stopColor="#cbd5e1" />
              <stop offset="100%" stopColor="#64748b" />
            </linearGradient>

            <linearGradient id="falconChromeFull" x1="0%" y1="0%" x2="100%" y2="50%">
              <stop offset="0%" stopColor="#e2e8f0" />
              <stop offset="50%" stopColor="#ffffff" />
              <stop offset="100%" stopColor="#94a3b8" />
            </linearGradient>

            <linearGradient id="falconRubberFull" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#334155" />
              <stop offset="50%" stopColor="#1e293b" />
              <stop offset="100%" stopColor="#0f172a" />
            </linearGradient>

            <linearGradient id="falconBoltFull" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#64748b" />
              <stop offset="40%" stopColor="#f8fafc" />
              <stop offset="100%" stopColor="#475569" />
            </linearGradient>
          </defs>

          {/* 1. Soaring Falcon Silhouette */}
          <g fill={color && color !== 'currentColor' ? color : 'url(#falconWingGradFull)'}>
            <path d="M 85 45 C 135 110 185 155 270 170 C 235 155 190 125 150 85 C 120 55 100 48 85 45 Z" />
            <path d="M 105 95 C 150 145 200 178 285 188 C 240 175 195 150 160 118 C 135 96 118 94 105 95 Z" />
            <path d="M 125 140 C 170 180 220 205 300 210 C 255 200 210 180 175 155 C 150 138 135 138 125 140 Z" />
            <path d="M 148 182 C 185 212 235 230 310 232 C 270 225 225 208 190 190 C 170 180 156 180 148 182 Z" />
            <path d="M 175 220 C 205 240 250 252 320 250 C 285 246 245 234 212 220 C 195 214 184 215 175 220 Z" />
            <path d="M 205 252 C 235 264 275 268 332 264 C 300 260 265 250 236 242 C 220 238 212 242 205 252 Z" />

            <path d="M 290 165 C 330 190 355 215 375 215 C 390 215 410 212 430 220 C 445 225 455 235 450 245 C 440 255 425 252 410 248 C 395 245 385 248 375 255 C 345 278 300 288 245 292 C 180 295 135 270 100 248 C 145 285 210 305 280 300 C 335 296 385 280 415 255 C 425 248 435 248 445 242 C 430 236 415 235 400 236 C 385 238 370 230 355 215 C 335 195 310 180 290 165 Z" />

            <path d="M 430 220 C 448 225 460 235 455 252 C 450 240 442 235 432 232 C 420 230 405 232 390 232 C 405 225 420 222 430 220 Z" />
            <circle cx="410" cy="232" r="3.5" fill="#ffffff" />
            <path d="M 404 230 Q 410 226 416 230 Q 410 234 404 230 Z" fill="currentColor" />

            <path d="M 120 270 C 180 310 260 322 340 300 C 280 315 200 305 145 278 C 132 272 124 270 120 270 Z" />
            <path d="M 160 298 C 220 330 300 332 365 308 C 310 324 235 322 180 304 C 170 300 164 298 160 298 Z" />
          </g>

          {/* 2. Ceiling Fan Downrod Pipe Assembly Directly Under Falcon */}
          <g transform="translate(115, 325) rotate(9.5 130 20)">
            {/* Rubber Vibration Damper Bushing */}
            <rect x="2" y="5" width="28" height="34" rx="5" fill="url(#falconRubberFull)" stroke="#334155" strokeWidth="1.5" />
            <rect x="5" y="10" width="22" height="6" rx="2" fill="#090d16" opacity="0.95" />
            <rect x="5" y="27" width="22" height="6" rx="2" fill="#090d16" opacity="0.95" />

            {/* Shackle Center Cross Bolt & Steel Bushing */}
            <circle cx="16" cy="22" r="7.5" fill="url(#falconBoltFull)" stroke="#1e293b" strokeWidth="1.5" />
            <circle cx="16" cy="22" r="4" fill="#0f172a" />
            <circle cx="15" cy="21" r="1.5" fill="#ffffff" opacity="0.6" />

            {/* Shackle Metal Clamping Yoke */}
            <path d="M 22 11 L 46 15 L 46 29 L 22 33 Z" fill="url(#falconChromeFull)" stroke="#64748b" strokeWidth="1.2" />
            <circle cx="36" cy="22" r="2.8" fill="#1e293b" stroke="#cbd5e1" strokeWidth="0.5" />

            {/* Main Steel Downrod Pipe Body */}
            <rect x="44" y="15" width="225" height="14" rx="7" fill="url(#falconRodGradFull)" stroke="#64748b" strokeWidth="1.2" />
            <line x1="48" y1="18.5" x2="260" y2="18.5" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round" opacity="0.95" />
            <line x1="48" y1="23.5" x2="260" y2="23.5" stroke="#94a3b8" strokeWidth="1" strokeLinecap="round" opacity="0.5" />
            <line x1="48" y1="27.5" x2="260" y2="27.5" stroke="#475569" strokeWidth="1.2" strokeLinecap="round" opacity="0.7" />

            {/* Downrod Right Tip Cotter Pin Hole */}
            <circle cx="254" cy="22" r="3.2" fill="#0f172a" stroke="#cbd5e1" strokeWidth="1" />
            <circle cx="254" cy="22" r="1.5" fill="#000000" />
            <circle cx="64" cy="22" r="2.2" fill="#1e293b" stroke="#cbd5e1" strokeWidth="0.5" />
          </g>

          {/* 3. Typography */}
          <g textAnchor="middle">
            <text
              x="250"
              y="465"
              fill="currentColor"
              fontFamily="'Playfair Display', 'Times New Roman', 'Georgia', serif"
              fontSize="44"
              fontWeight="700"
              letterSpacing="1.5"
            >
              {companyName}
            </text>
            <text
              x="250"
              y="500"
              fill="currentColor"
              fontFamily="'IBM Plex Sans', 'Helvetica Neue', 'Arial', sans-serif"
              fontSize="19"
              fontWeight="600"
              letterSpacing="9"
              opacity="0.9"
            >
              {companyTagline}
            </text>
          </g>
        </svg>
      </div>
    );
  }

  // Pure Emblem (Falcon + Downrod, no bottom text) - Perfect for TopBar, Header, Favicon, Sidebar
  return (
    <div 
      onClick={onClick}
      className={`inline-flex items-center justify-center select-none ${onClick ? 'cursor-pointer' : ''} ${className}`}
    >
      <svg
        viewBox="0 0 500 420"
        width={numericSize}
        height={numericSize * 0.84}
        className="w-auto h-auto max-w-full drop-shadow-sm transition-transform duration-200"
        style={{ color: color || 'currentColor' }}
        role="img"
        aria-label={altText}
      >
          <defs>
            <linearGradient id="falconWingGradEmb" x1="10%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FEF3C7" />
              <stop offset="25%" stopColor="#FBBF24" />
              <stop offset="60%" stopColor="#F59E0B" />
              <stop offset="85%" stopColor="#D97706" />
              <stop offset="100%" stopColor="#92400E" />
            </linearGradient>

            <linearGradient id="falconRodGradEmb" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#94a3b8" />
            <stop offset="25%" stopColor="#e2e8f0" />
            <stop offset="50%" stopColor="#ffffff" />
            <stop offset="75%" stopColor="#cbd5e1" />
            <stop offset="100%" stopColor="#64748b" />
          </linearGradient>

          <linearGradient id="falconChromeEmb" x1="0%" y1="0%" x2="100%" y2="50%">
            <stop offset="0%" stopColor="#e2e8f0" />
            <stop offset="50%" stopColor="#ffffff" />
            <stop offset="100%" stopColor="#94a3b8" />
          </linearGradient>

          <linearGradient id="falconRubberEmb" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#334155" />
            <stop offset="50%" stopColor="#1e293b" />
            <stop offset="100%" stopColor="#0f172a" />
          </linearGradient>

          <linearGradient id="falconBoltEmb" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#64748b" />
            <stop offset="40%" stopColor="#f8fafc" />
            <stop offset="100%" stopColor="#475569" />
          </linearGradient>
        </defs>

        {/* Soaring Falcon Silhouette */}
        <g fill={color && color !== 'currentColor' ? color : 'url(#falconWingGradEmb)'}>
          <path d="M 85 45 C 135 110 185 155 270 170 C 235 155 190 125 150 85 C 120 55 100 48 85 45 Z" />
          <path d="M 105 95 C 150 145 200 178 285 188 C 240 175 195 150 160 118 C 135 96 118 94 105 95 Z" />
          <path d="M 125 140 C 170 180 220 205 300 210 C 255 200 210 180 175 155 C 150 138 135 138 125 140 Z" />
          <path d="M 148 182 C 185 212 235 230 310 232 C 270 225 225 208 190 190 C 170 180 156 180 148 182 Z" />
          <path d="M 175 220 C 205 240 250 252 320 250 C 285 246 245 234 212 220 C 195 214 184 215 175 220 Z" />
          <path d="M 205 252 C 235 264 275 268 332 264 C 300 260 265 250 236 242 C 220 238 212 242 205 252 Z" />

          <path d="M 290 165 C 330 190 355 215 375 215 C 390 215 410 212 430 220 C 445 225 455 235 450 245 C 440 255 425 252 410 248 C 395 245 385 248 375 255 C 345 278 300 288 245 292 C 180 295 135 270 100 248 C 145 285 210 305 280 300 C 335 296 385 280 415 255 C 425 248 435 248 445 242 C 430 236 415 235 400 236 C 385 238 370 230 355 215 C 335 195 310 180 290 165 Z" />

          <path d="M 430 220 C 448 225 460 235 455 252 C 450 240 442 235 432 232 C 420 230 405 232 390 232 C 405 225 420 222 430 220 Z" />
          <circle cx="410" cy="232" r="3.5" fill="#ffffff" />
          <path d="M 404 230 Q 410 226 416 230 Q 410 234 404 230 Z" fill="currentColor" />

          <path d="M 120 270 C 180 310 260 322 340 300 C 280 315 200 305 145 278 C 132 272 124 270 120 270 Z" />
          <path d="M 160 298 C 220 330 300 332 365 308 C 310 324 235 322 180 304 C 170 300 164 298 160 298 Z" />
        </g>

        {/* Ceiling Fan Downrod Assembly Directly Under Falcon */}
        <g transform="translate(115, 325) rotate(9.5 130 20)">
          <rect x="2" y="5" width="28" height="34" rx="5" fill="url(#falconRubberEmb)" stroke="#334155" strokeWidth="1.5" />
          <rect x="5" y="10" width="22" height="6" rx="2" fill="#090d16" opacity="0.95" />
          <rect x="5" y="27" width="22" height="6" rx="2" fill="#090d16" opacity="0.95" />

          <circle cx="16" cy="22" r="7.5" fill="url(#falconBoltEmb)" stroke="#1e293b" strokeWidth="1.5" />
          <circle cx="16" cy="22" r="4" fill="#0f172a" />
          <circle cx="15" cy="21" r="1.5" fill="#ffffff" opacity="0.6" />

          <path d="M 22 11 L 46 15 L 46 29 L 22 33 Z" fill="url(#falconChromeEmb)" stroke="#64748b" strokeWidth="1.2" />
          <circle cx="36" cy="22" r="2.8" fill="#1e293b" stroke="#cbd5e1" strokeWidth="0.5" />

          <rect x="44" y="15" width="225" height="14" rx="7" fill="url(#falconRodGradEmb)" stroke="#64748b" strokeWidth="1.2" />
          <line x1="48" y1="18.5" x2="260" y2="18.5" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round" opacity="0.95" />
          <line x1="48" y1="23.5" x2="260" y2="23.5" stroke="#94a3b8" strokeWidth="1" strokeLinecap="round" opacity="0.5" />
          <line x1="48" y1="27.5" x2="260" y2="27.5" stroke="#475569" strokeWidth="1.2" strokeLinecap="round" opacity="0.7" />

          <circle cx="254" cy="22" r="3.2" fill="#0f172a" stroke="#cbd5e1" strokeWidth="1" />
          <circle cx="254" cy="22" r="1.5" fill="#000000" />
          <circle cx="64" cy="22" r="2.2" fill="#1e293b" stroke="#cbd5e1" strokeWidth="0.5" />
        </g>
      </svg>
    </div>
  );
};

export default FalconLogo;

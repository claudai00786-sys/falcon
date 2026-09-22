import React, { useState, useMemo } from 'react';
import {
  TrendingUp,
  CreditCard,
  Layers,
  ChevronDown,
  ChevronUp,
  Coins,
  BarChart3,
  PieChart,
  Calendar,
  Activity,
  ArrowUpRight,
  ArrowDownRight,
  ShieldAlert,
  Boxes,
  Users
} from 'lucide-react';
import { AppState, AppLanguage } from '../types';
import { fmt, parseDMY } from '../utils/helpers';
import { computeSystemFinancials, computeCustomerLedgerDetails } from '../utils/mathEngine';

interface OverviewViewProps {
  state: AppState;
  language: AppLanguage;
  onNavigate: (view: any) => void;
  onOpenCustomerLedger: (factoryName: string) => void;
}

export const OverviewView: React.FC<OverviewViewProps> = ({
  state,
  language,
  onNavigate,
  onOpenCustomerLedger
}) => {
  const [calendarExpanded, setCalendarExpanded] = useState(false);
  const [salesPeriod, setSalesPeriod] = useState('monthly');
  const [summaryPeriod, setSummaryPeriod] = useState('monthly');
  const [chartTimespan, setChartTimespan] = useState<'7days' | '30days'>('7days');
  const [activeChartBar, setActiveChartBar] = useState<number | null>(null);

  // Calendar Calculation
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth();
  const today = now.getDate();
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const firstDow = (new Date(currentYear, currentMonth, 1).getDay() + 6) % 7;

  // Compute live mathematical financials using unified math engine
  const fin = computeSystemFinancials(state, salesPeriod, summaryPeriod);

  // Leaderboard of who owes the most (Receivables)
  const debtorFactories = useMemo(() => {
    return state.customerLedgers
      .map(cl => {
        const details = computeCustomerLedgerDetails(cl.entries);
        return { name: cl.name, balance: details.netBalance };
      })
      .filter(f => f.balance > 0)
      .sort((a, b) => b.balance - a.balance)
      .slice(0, 4);
  }, [state.customerLedgers]);

  // Recent activity log
  const recentActivities = useMemo(() => {
    return [
      ...state.transactions.slice(0, 4).map(t => ({
        id: `tx_${t.id}`,
        type: 'sale',
        title: `Order #${t.id} - ${t.factory || 'Walk-in'}`,
        titleUr: `آرڈر #${t.id} - ${t.factory || 'واک ان گاہک'}`,
        sub: `${t.date} · ${t.itemsSummary.replace(/\n/g, ', ')}`,
        amount: t.total,
        positive: t.paid
      })),
      ...state.expenses.slice(0, 4).map(e => ({
        id: `exp_${e.id}`,
        type: 'expense',
        title: `${e.category}: ${e.desc}`,
        titleUr: `خرچ: ${e.category} (${e.desc})`,
        sub: `${e.date} · ${e.method}`,
        amount: e.amount,
        positive: false
      }))
    ].slice(0, 6);
  }, [state.transactions, state.expenses]);

  // Robust date parser
  const parseDateRobust = (dStr: string | null | undefined): Date | null => {
    if (!dStr) return null;
    const p = parseDMY(dStr);
    if (p && !isNaN(p.getTime())) return p;
    const d = new Date(dStr);
    return isNaN(d.getTime()) ? null : d;
  };

  // Build daily timeline data for the bilingual chart
  const chartData = useMemo(() => {
    const daysCount = chartTimespan === '7days' ? 7 : 30;
    const days = [];
    const urduDays = ['اتوار', 'پیر', 'منگل', 'بدھ', 'جمعرات', 'جمعہ', 'ہفتہ'];
    const engDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    for (let i = daysCount - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      d.setHours(0, 0, 0, 0);

      const dayNum = d.getDate();
      const monthNum = d.getMonth() + 1;
      const dateKeyDMY = `${String(dayNum).padStart(2, '0')}/${String(monthNum).padStart(2, '0')}/${d.getFullYear()}`;
      const dayOfWeek = d.getDay();

      // Sum sales for this day
      const daySales = state.transactions
        .filter(t => {
          const tDate = parseDateRobust(t.date);
          if (!tDate) return false;
          return (
            tDate.getDate() === d.getDate() &&
            tDate.getMonth() === d.getMonth() &&
            tDate.getFullYear() === d.getFullYear()
          );
        })
        .reduce((sum, t) => sum + (t.total || 0), 0);

      // Sum expenses for this day
      const dayExpenses = state.expenses
        .filter(e => {
          const eDate = parseDateRobust(e.date);
          if (!eDate) return false;
          return (
            eDate.getDate() === d.getDate() &&
            eDate.getMonth() === d.getMonth() &&
            eDate.getFullYear() === d.getFullYear()
          );
        })
        .reduce((sum, e) => sum + (e.amount || 0), 0);

      const netProfit = daySales - dayExpenses;

      days.push({
        date: dateKeyDMY,
        dayLabel: `${dayNum}/${monthNum}`,
        engDay: engDays[dayOfWeek],
        urduDay: urduDays[dayOfWeek],
        sales: daySales,
        expenses: dayExpenses,
        netProfit
      });
    }

    const maxVal = Math.max(...days.map(d => Math.max(d.sales, d.expenses)), 1000);

    return {
      days,
      maxVal,
      totalSales: days.reduce((acc, d) => acc + d.sales, 0),
      totalExpenses: days.reduce((acc, d) => acc + d.expenses, 0),
      totalNet: days.reduce((acc, d) => acc + d.netProfit, 0)
    };
  }, [state.transactions, state.expenses, chartTimespan]);

  // Product category breakdown (Ceiling vs Pedestal)
  const productMix = useMemo(() => {
    let ceilingStock = 0;
    let ceilingVal = 0;
    let pedestalStock = 0;
    let pedestalVal = 0;

    state.products.forEach(p => {
      const isPedestal = (p.cat || p.name || '').toLowerCase().includes('pedestal');
      const qty = p.stock || 0;
      const price = p.price || 0;
      if (isPedestal) {
        pedestalStock += qty;
        pedestalVal += qty * price;
      } else {
        ceilingStock += qty;
        ceilingVal += qty * price;
      }
    });

    const totalStock = ceilingStock + pedestalStock;
    const ceilingPct = totalStock > 0 ? Math.round((ceilingStock / totalStock) * 100) : 50;
    const pedestalPct = totalStock > 0 ? 100 - ceilingPct : 50;

    return {
      ceilingStock,
      ceilingVal,
      ceilingPct,
      pedestalStock,
      pedestalVal,
      pedestalPct,
      totalStock
    };
  }, [state.products]);

  // Helper for dual English + Urdu labels
  const renderBilingual = (en: string, ur: string, className = '', urClassName = '') => (
    <div className={`flex flex-col text-left ${className}`}>
      <span className="leading-tight">{en}</span>
      <span className={`text-[10.5px] font-serif opacity-80 leading-tight -mt-0.5 ${urClassName}`}>{ur}</span>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header with Bilingual Brand & Title */}
      <div className="flex items-center justify-between flex-wrap gap-4 border-b border-[var(--steel-line)] pb-4">
        <div className="flex items-center gap-3">
          <div className="bg-white rounded-xl p-1.5 px-3 shadow-xs border border-slate-200/80 flex items-center justify-center shrink-0 hover:border-amber-400 transition-colors">
            <img
              src="/falcon-logo.png"
              alt="Falcon Rod Maker"
              className="h-10 w-auto object-contain select-none animate-logo-glow"
            />
          </div>
          <div>
            <div className="flex items-baseline gap-2">
              <h2 className="font-serif font-black text-2xl text-[var(--text)]">Business Overview</h2>
              <span className="font-serif text-lg font-bold text-[var(--yellow)]">کاروباری جائزہ</span>
            </div>
            <p className="text-xs text-[var(--text-dim)] mt-0.5 flex items-center gap-2">
              <span>Real-time Financial Analytics & POS Performance</span>
              <span className="opacity-40">|</span>
              <span className="font-serif text-[11px] opacity-80">براہِ راست مالیاتی تجزیہ اور ورکشاپ حسابات</span>
            </p>
          </div>
        </div>

        {/* Quick Jump Badges */}
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onNavigate('sales')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[var(--steel-line)] bg-[var(--panel-raised)] hover:border-[var(--yellow)] text-xs font-semibold text-[var(--text)] transition cursor-pointer"
          >
            <TrendingUp size={14} className="text-[var(--yellow)]" />
            {renderBilingual('Sales POS', 'سیلز کاؤنٹر')}
          </button>
          <button
            type="button"
            onClick={() => onNavigate('stock')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[var(--steel-line)] bg-[var(--panel-raised)] hover:border-[var(--yellow)] text-xs font-semibold text-[var(--text)] transition cursor-pointer"
          >
            <Boxes size={14} className="text-emerald-400" />
            {renderBilingual('Stock Room', 'اسٹاک گودام')}
          </button>
        </div>
      </div>

      {/* Month Calendar Panel (Bilingual) */}
      <div
        onClick={() => setCalendarExpanded(!calendarExpanded)}
        className={`bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-xl p-4 cursor-pointer transition-all duration-300 ${
          calendarExpanded ? 'w-full shadow-lg' : 'w-fit'
        }`}
      >
        <div className="flex items-center gap-6">
          <div className="shrink-0">
            <div className="font-mono font-bold text-2xl text-[var(--text)] leading-none">
              {now.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
            </div>
            <div className="font-mono text-xs uppercase tracking-wider text-[var(--yellow)] mt-1 font-semibold flex items-center gap-2">
              <span>{now.toLocaleDateString('en-GB', { weekday: 'long' })}</span>
              <span className="font-serif font-normal normal-case text-amber-300 text-xs">
                {['اتوار', 'پیر', 'منگل', 'بدھ', 'جمعرات', 'جمعہ', 'ہفتہ'][now.getDay()]}
              </span>
            </div>
          </div>
          <div className="text-xs text-[var(--text-dim)] flex items-center gap-1.5 font-mono">
            <span>{calendarExpanded ? 'Collapse Calendar' : 'Expand Month'}</span>
            <span className="font-serif opacity-75">({calendarExpanded ? 'بند کریں' : 'کیلنڈر کھولیں'})</span>
            {calendarExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          </div>
        </div>

        {calendarExpanded && (
          <div className="mt-4 pt-4 border-t border-[var(--steel-line)]">
            <div className="flex items-center justify-between mb-2">
              <div className="text-xs font-mono font-bold text-[var(--yellow)] uppercase tracking-wider">
                {now.toLocaleDateString('en-GB', { month: 'long', year: 'numeric' })}
              </div>
              <span className="font-serif text-xs text-amber-300">
                ماہانہ کیلنڈر و تاریخ
              </span>
            </div>
            <div className="grid grid-cols-7 gap-1.5 max-w-sm text-center font-mono">
              {[
                { en: 'M', ur: 'پ' },
                { en: 'T', ur: 'م' },
                { en: 'W', ur: 'ب' },
                { en: 'T', ur: 'ج' },
                { en: 'F', ur: 'ج' },
                { en: 'S', ur: 'ہ' },
                { en: 'S', ur: 'ا' }
              ].map((d, idx) => (
                <div key={idx} className="text-[10px] text-[var(--text-dim)] py-0.5 font-bold flex flex-col items-center">
                  <span>{d.en}</span>
                  <span className="font-serif text-[9px] opacity-70 -mt-0.5">{d.ur}</span>
                </div>
              ))}
              {Array.from({ length: firstDow }).map((_, i) => (
                <div key={`blank_${i}`} />
              ))}
              {Array.from({ length: daysInMonth }).map((_, i) => {
                const dayNum = i + 1;
                const isToday = dayNum === today;
                return (
                  <div
                    key={dayNum}
                    className={`py-1 text-xs rounded transition-all ${
                      isToday ? 'bg-[var(--yellow)] text-black font-bold shadow-md scale-105' : 'text-[var(--text)] hover:bg-[var(--panel)]'
                    }`}
                  >
                    {dayNum}
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* ========================================================================= */}
      {/* SECTION: BILINGUAL INTERACTIVE CHARTS & VISUAL ANALYTICS */}
      {/* ========================================================================= */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Main Chart: Financial Performance & Sales vs Expenses Trend */}
        <div className="lg:col-span-2 bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-5 shadow-sm flex flex-col justify-between">
          <div>
            {/* Chart Header & Timespan Toggle */}
            <div className="flex items-center justify-between flex-wrap gap-3 mb-4">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold border border-amber-500/30">
                  <BarChart3 size={18} />
                </div>
                <div>
                  <div className="flex items-baseline gap-2">
                    <h3 className="font-serif font-bold text-base text-[var(--text)]">Sales & Expenses Trend</h3>
                    <span className="font-serif text-sm font-semibold text-[var(--yellow)]">سیلز اور اخراجات کا رجحان</span>
                  </div>
                  <p className="text-[11px] text-[var(--text-dim)] font-mono">
                    Daily comparative revenue vs workshop operational costs
                  </p>
                </div>
              </div>

              {/* Bilingual Timespan Switcher */}
              <div className="flex items-center bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg p-0.5">
                <button
                  type="button"
                  onClick={() => setChartTimespan('7days')}
                  className={`px-2.5 py-1 rounded-md text-xs font-mono font-bold transition cursor-pointer ${
                    chartTimespan === '7days'
                      ? 'bg-[var(--yellow)] text-black shadow-xs'
                      : 'text-[var(--text-dim)] hover:text-[var(--text)]'
                  }`}
                >
                  <span className="inline-flex items-center gap-1">
                    <span>7 Days</span>
                    <span className="font-serif text-[10px] font-normal">(۷ دن)</span>
                  </span>
                </button>
                <button
                  type="button"
                  onClick={() => setChartTimespan('30days')}
                  className={`px-2.5 py-1 rounded-md text-xs font-mono font-bold transition cursor-pointer ${
                    chartTimespan === '30days'
                      ? 'bg-[var(--yellow)] text-black shadow-xs'
                      : 'text-[var(--text-dim)] hover:text-[var(--text)]'
                  }`}
                >
                  <span className="inline-flex items-center gap-1">
                    <span>30 Days</span>
                    <span className="font-serif text-[10px] font-normal">(۳۰ دن)</span>
                  </span>
                </button>
              </div>
            </div>

            {/* Bilingual Legend & Summary Bar */}
            <div className="flex items-center justify-between flex-wrap gap-4 py-2 px-3 rounded-lg bg-[var(--panel-raised)]/70 border border-[var(--steel-line)] mb-4 text-xs font-mono">
              <div className="flex items-center gap-4 flex-wrap">
                {/* Sales Legend */}
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded-xs bg-amber-400 shrink-0" />
                  <span className="font-semibold text-amber-300">Sales Turnover</span>
                  <span className="font-serif text-[11px] text-amber-200/80">(سیلز آمدن)</span>
                </div>
                {/* Expenses Legend */}
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded-xs bg-rose-500 shrink-0" />
                  <span className="font-semibold text-rose-300">Expenses</span>
                  <span className="font-serif text-[11px] text-rose-200/80">(اخراجات)</span>
                </div>
                {/* Net Profit Legend */}
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded-xs bg-emerald-400 shrink-0" />
                  <span className="font-semibold text-emerald-300">Net Flow</span>
                  <span className="font-serif text-[11px] text-emerald-200/80">(خالص کیش)</span>
                </div>
              </div>

              {/* Totals Pill */}
              <div className="text-[11px] font-bold text-[var(--text-dim)]">
                Net: <span className={chartData.totalNet >= 0 ? 'text-emerald-400' : 'text-rose-400'}>
                  {chartData.totalNet >= 0 ? '+' : ''}{fmt(chartData.totalNet)}
                </span>
              </div>
            </div>

            {/* Interactive Bilingual Chart Area */}
            <div className="relative pt-6 pb-2">
              <div className="h-48 w-full flex items-end gap-1.5 sm:gap-2.5 px-2 border-b border-[var(--steel-line)]">
                {chartData.days.map((item, idx) => {
                  const salesHeightPct = Math.max(Math.min((item.sales / chartData.maxVal) * 100, 100), 3);
                  const expHeightPct = Math.max(Math.min((item.expenses / chartData.maxVal) * 100, 100), 3);
                  const isHovered = activeChartBar === idx;

                  return (
                    <div
                      key={idx}
                      onMouseEnter={() => setActiveChartBar(idx)}
                      onMouseLeave={() => setActiveChartBar(null)}
                      className="flex-1 flex flex-col items-center justify-end h-full relative group cursor-pointer"
                    >
                      {/* Hover Tooltip (Bilingual) */}
                      {isHovered && (
                        <div className="absolute -top-20 z-20 bg-slate-900 text-white rounded-lg p-2.5 shadow-xl border border-amber-500/40 text-[11px] font-mono min-w-36 pointer-events-none whitespace-nowrap">
                          <div className="font-bold border-b border-slate-700 pb-1 mb-1 flex items-center justify-between text-amber-300">
                            <span>{item.date} ({item.engDay})</span>
                            <span className="font-serif">{item.urduDay}</span>
                          </div>
                          <div className="flex justify-between text-amber-400">
                            <span>Sales (سیلز):</span>
                            <span className="font-bold">{fmt(item.sales)}</span>
                          </div>
                          <div className="flex justify-between text-rose-400">
                            <span>Expenses (خرچ):</span>
                            <span className="font-bold">{fmt(item.expenses)}</span>
                          </div>
                          <div className="flex justify-between text-emerald-400 border-t border-slate-700/60 pt-0.5 mt-0.5">
                            <span>Net (خالص):</span>
                            <span className="font-bold">{fmt(item.netProfit)}</span>
                          </div>
                        </div>
                      )}

                      {/* Paired Bar Columns */}
                      <div className="w-full flex items-end justify-center gap-0.5 sm:gap-1 h-full">
                        {/* Sales Bar */}
                        <div
                          style={{ height: `${salesHeightPct}%` }}
                          className={`w-full max-w-[14px] rounded-t-sm transition-all duration-300 ${
                            isHovered
                              ? 'bg-amber-300 shadow-[0_0_8px_rgba(251,191,36,0.8)]'
                              : 'bg-amber-500 hover:bg-amber-400'
                          }`}
                        />
                        {/* Expenses Bar */}
                        <div
                          style={{ height: `${expHeightPct}%` }}
                          className={`w-full max-w-[14px] rounded-t-sm transition-all duration-300 ${
                            isHovered
                              ? 'bg-rose-400 shadow-[0_0_8px_rgba(244,63,94,0.8)]'
                              : 'bg-rose-600 hover:bg-rose-500'
                          }`}
                        />
                      </div>

                      {/* X-Axis Bilingual Day Labels */}
                      <div className="mt-2 text-center select-none">
                        <div className="text-[10px] font-mono text-[var(--text)] font-semibold leading-tight">
                          {chartTimespan === '7days' ? item.engDay : item.dayLabel}
                        </div>
                        <div className="text-[9.5px] font-serif text-[var(--yellow)] opacity-85 leading-tight">
                          {item.urduDay}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Chart Footnote Details */}
          <div className="mt-3 pt-3 border-t border-[var(--steel-line)] flex items-center justify-between text-xs font-mono text-[var(--text-dim)]">
            <div className="flex items-center gap-1.5">
              <Activity size={13} className="text-emerald-400" />
              <span>Period Sales Volume: <strong className="text-amber-400">{fmt(chartData.totalSales)}</strong></span>
            </div>
            <div className="font-serif text-[11px] text-amber-300/90">
              کل فروخت: {fmt(chartData.totalSales)} | کل اخراجات: {fmt(chartData.totalExpenses)}
            </div>
          </div>
        </div>

        {/* Secondary Chart: Fan Rods Product Mix & Finished Inventory Breakdown */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-5 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2.5 mb-4">
              <div className="w-8 h-8 rounded-lg bg-teal-500/20 text-teal-400 flex items-center justify-center font-bold border border-teal-500/30">
                <PieChart size={18} />
              </div>
              <div>
                <div className="flex items-baseline gap-2">
                  <h3 className="font-serif font-bold text-base text-[var(--text)]">Fan Rods Mix</h3>
                  <span className="font-serif text-sm font-semibold text-teal-300">پروڈکٹ کیٹلاگ تناسب</span>
                </div>
                <p className="text-[11px] text-[var(--text-dim)] font-mono">
                  Finished inventory allocation & stock value
                </p>
              </div>
            </div>

            {/* Visual Split Ratio Bar */}
            <div className="space-y-2 mb-4">
              <div className="flex justify-between items-center text-xs font-mono">
                <span className="text-amber-400 font-bold">
                  Ceiling Rods <span className="font-serif text-[11px] font-normal text-amber-300/80">(سیلنگ {productMix.ceilingPct}%)</span>
                </span>
                <span className="text-sky-400 font-bold">
                  Pedestal Rods <span className="font-serif text-[11px] font-normal text-sky-300/80">(پیڈسٹل {productMix.pedestalPct}%)</span>
                </span>
              </div>
              <div className="h-3 w-full rounded-full bg-slate-800 overflow-hidden flex p-0.5 border border-[var(--steel-line)]">
                <div
                  style={{ width: `${productMix.ceilingPct}%` }}
                  className="h-full bg-amber-400 rounded-l-full transition-all duration-500 shadow-[0_0_8px_rgba(251,191,36,0.5)]"
                />
                <div
                  style={{ width: `${productMix.pedestalPct}%` }}
                  className="h-full bg-sky-400 rounded-r-full transition-all duration-500 shadow-[0_0_8px_rgba(56,189,248,0.5)]"
                />
              </div>
            </div>

            {/* Bilingual Category Cards */}
            <div className="space-y-2.5 font-mono text-xs">
              {/* Ceiling Fan Rods */}
              <div className="p-3 rounded-lg bg-[var(--panel-raised)] border border-amber-500/30 flex items-center justify-between">
                <div>
                  <div className="flex items-baseline gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full bg-amber-400 shrink-0" />
                    <span className="font-bold text-[var(--text)]">Ceiling Fan Rods</span>
                  </div>
                  <span className="font-serif text-[11px] text-amber-300 ml-4 block">سیلنگ فین راڈز</span>
                </div>
                <div className="text-right">
                  <div className="font-bold text-amber-400 text-sm">{productMix.ceilingStock} Pcs (عدد)</div>
                  <div className="text-[10px] text-[var(--text-dim)]">{fmt(productMix.ceilingVal)}</div>
                </div>
              </div>

              {/* Pedestal Fan Rods */}
              <div className="p-3 rounded-lg bg-[var(--panel-raised)] border border-sky-500/30 flex items-center justify-between">
                <div>
                  <div className="flex items-baseline gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full bg-sky-400 shrink-0" />
                    <span className="font-bold text-[var(--text)]">Pedestal Fan Rods</span>
                  </div>
                  <span className="font-serif text-[11px] text-sky-300 ml-4 block">پیڈسٹل فین راڈز</span>
                </div>
                <div className="text-right">
                  <div className="font-bold text-sky-400 text-sm">{productMix.pedestalStock} Pcs (عدد)</div>
                  <div className="text-[10px] text-[var(--text-dim)]">{fmt(productMix.pedestalVal)}</div>
                </div>
              </div>

              {/* Cash vs Receivables Gauge */}
              <div className="p-3 rounded-lg bg-[var(--panel-raised)] border border-[var(--steel-line)] space-y-1.5">
                <div className="flex justify-between items-baseline text-[11px]">
                  <span className="text-[var(--text-dim)]">Liquid Cash vs Market Owed</span>
                  <span className="font-serif text-emerald-400 text-[11px]">نقد کیش بنام بقایا</span>
                </div>
                <div className="flex justify-between items-baseline font-bold">
                  <span className="text-emerald-400">Cash (نقد): {fmt(fin.cashInHand)}</span>
                  <span className="text-rose-400">Owed (بقایا): {fmt(fin.totalReceivables)}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="pt-3 border-t border-[var(--steel-line)]">
            <button
              type="button"
              onClick={() => onNavigate('products')}
              className="w-full py-2 rounded-lg bg-[var(--panel-raised)] hover:border-[var(--yellow)] border border-[var(--steel-line)] text-xs text-[var(--text)] hover:text-[var(--yellow)] transition text-center font-semibold"
            >
              Open Products Catalog (پروڈکٹ کیٹلاگ) →
            </button>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* SECTION: PRIMARY BENTO KPI MODULES (FULLY BILINGUAL) */}
      {/* ========================================================================= */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Module 1: Live Cash In Hand (Tijori / Drawer) */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="p-4 bg-gradient-to-br from-emerald-500/20 via-transparent to-transparent border-b border-[var(--steel-line)] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-emerald-500 text-black flex items-center justify-center font-bold">
                <Coins size={16} />
              </div>
              <div>
                <h3 className="font-semibold text-sm text-[var(--text)]">Cash Register</h3>
                <span className="font-serif text-[11px] text-emerald-300 font-bold block -mt-0.5">تجوری و نقد رقم</span>
              </div>
            </div>
            <span className="text-[10px] font-mono font-bold uppercase text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/30">
              Live Cash
            </span>
          </div>

          <div className="p-4 space-y-2.5 flex-1 font-mono text-xs">
            <div className="flex justify-between items-baseline">
              {renderBilingual('Total Cash Inflow', 'کل کیش آمد')}
              <span className="font-semibold text-[var(--green)]">+{fmt(fin.cashInflow)}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Total Cash Outflow', 'کل کیش اخراجات')}
              <span className="font-semibold text-[var(--red)]">-{fmt(fin.cashOutflow)}</span>
            </div>
            <div className="border-t border-[var(--steel-line)] pt-2 flex justify-between items-baseline">
              {renderBilingual('Cash In Hand', 'کل نقد (ہاتھ میں)', 'font-bold text-[var(--text)]')}
              <span
                className={`font-bold text-base ${
                  fin.cashInHand >= 0 ? 'text-[var(--green)]' : 'text-[var(--red)]'
                }`}
              >
                {fmt(fin.cashInHand)}
              </span>
            </div>
            <div className="border-t border-[var(--steel-line)] pt-2 space-y-1.5 text-[11px]">
              <div className="flex justify-between items-baseline">
                {renderBilingual('Finished Stock Value', 'تیار راڈز مالیت')}
                <span className="text-[var(--yellow)] font-semibold">{fmt(fin.finishedGoodsValue)}</span>
              </div>
              <div className="flex justify-between items-baseline">
                {renderBilingual('Raw Stock Value', 'خام مال مالیت')}
                <span className="text-[var(--text)] font-semibold">{fmt(fin.rawStockValue)}</span>
              </div>
              <div className="flex justify-between items-baseline pt-1 border-t border-[var(--steel-line)]/50">
                {renderBilingual('Net Workshop Assets', 'کارخانہ کے کل اثاثے', 'font-bold text-[var(--text-dim)]')}
                <span className="font-bold text-emerald-400">{fmt(fin.totalAssetValuation)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Module 2: Business Summary Report */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="p-4 bg-gradient-to-br from-amber-500/20 via-transparent to-transparent border-b border-[var(--steel-line)] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-[var(--yellow)] text-black flex items-center justify-center font-bold">
                <TrendingUp size={16} />
              </div>
              <div>
                <h3 className="font-semibold text-sm text-[var(--text)]">Business Summary</h3>
                <span className="font-serif text-[11px] text-amber-300 font-bold block -mt-0.5">کاروباری خلاصہ</span>
              </div>
            </div>
            <select
              value={summaryPeriod}
              onChange={e => setSummaryPeriod(e.target.value)}
              className="bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[10px] font-mono rounded px-2 py-1 text-[var(--text)]"
            >
              <option value="daily">Daily (روزانہ)</option>
              <option value="weekly">Weekly (ہفتہ وار)</option>
              <option value="monthly">Monthly (ماہانہ)</option>
              <option value="yearly">Yearly (سالانہ)</option>
              <option value="all">All Time (مکمل)</option>
            </select>
          </div>

          <div className="p-4 space-y-2 flex-1 font-mono text-xs">
            <div className="flex justify-between items-baseline">
              {renderBilingual('Period Sales', 'مدت کی سیلز')}
              <span className="font-bold text-[var(--yellow)]">{fmt(fin.summarySalesTotal)}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Total Expenses', 'کل اخراجات')}
              <span className="font-bold text-[var(--red)]">{fmt(fin.periodExpensesTotal)}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Raw Material Cost', 'خام مال لاگت')}
              <span className="font-semibold">{fmt(fin.filteredMaterialPayments)}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Labour & Paint Cost', 'مزدوری و پینٹ لاگت')}
              <span className="font-semibold">{fmt(fin.filteredLabourPayments + fin.filteredPainterPayments)}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Owner Withdrawals', 'ذاتی نکاسی')}
              <span className="font-semibold text-amber-500">{fmt(fin.periodWithdrawalsTotal)}</span>
            </div>
            <div className="border-t border-[var(--steel-line)] pt-2 flex justify-between items-baseline">
              {renderBilingual('Net Profit', 'خالص منافع', 'font-bold text-[var(--text)]')}
              <span
                className={`font-bold text-sm ${
                  fin.periodNetProfit >= 0 ? 'text-[var(--green)]' : 'text-[var(--red)]'
                }`}
              >
                {fmt(fin.periodNetProfit)}
              </span>
            </div>
            <div className="flex justify-between items-baseline text-[11px] pt-1">
              {renderBilingual('Market Receivables', 'مارکیٹ سے وصولی')}
              <span className="text-[var(--green)] font-semibold">{fmt(fin.totalReceivables)}</span>
            </div>
            <div className="flex justify-between items-baseline text-[11px]">
              {renderBilingual('Supplier Payables', 'سپلائر کو واجب الادا')}
              <span className="text-[var(--red)] font-semibold">{fmt(fin.totalPayables)}</span>
            </div>
          </div>
        </div>

        {/* Module 3: Sales & Orders Module */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="p-4 bg-gradient-to-br from-cyan-500/20 via-transparent to-transparent border-b border-[var(--steel-line)] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-cyan-500 text-black flex items-center justify-center font-bold">
                <CreditCard size={16} />
              </div>
              <div>
                <h3 className="font-semibold text-sm text-[var(--text)]">Sales & Orders</h3>
                <span className="font-serif text-[11px] text-cyan-300 font-bold block -mt-0.5">سیلز و آرڈرز</span>
              </div>
            </div>
            <select
              value={salesPeriod}
              onChange={e => setSalesPeriod(e.target.value)}
              className="bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[10px] font-mono rounded px-2 py-1 text-[var(--text)]"
            >
              <option value="daily">Daily (روزانہ)</option>
              <option value="weekly">Weekly (ہفتہ وار)</option>
              <option value="monthly">Monthly (ماہانہ)</option>
              <option value="yearly">Yearly (سالانہ)</option>
              <option value="all">All Time (مکمل)</option>
            </select>
          </div>

          <div className="p-4 space-y-2.5 flex-1 font-mono text-xs">
            <div className="flex justify-between items-baseline">
              {renderBilingual('Period Turnover', 'سیلز ٹرن اوور')}
              <span className="font-bold text-sm text-[var(--yellow)]">{fmt(fin.periodSalesTotal)}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Orders Completed', 'مکمل آرڈرز')}
              <span className="font-bold text-base text-[var(--text)]">{fin.periodOrdersCount}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Total Rods Sold', 'فروخت شدہ راڈز')}
              <span className="font-bold text-base text-[var(--text)]">{fin.periodItemsSold}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Average Sale Value', 'اوسط آرڈر رقم')}
              <span className="font-bold text-sm text-[var(--yellow)]">{fmt(fin.periodAvgSale)}</span>
            </div>
            <div className="border-t border-[var(--steel-line)] pt-3">
              <button
                type="button"
                onClick={() => onNavigate('transactions')}
                className="w-full py-2 rounded-lg bg-[var(--panel-raised)] hover:border-[var(--yellow)] border border-[var(--steel-line)] text-xs text-[var(--text-dim)] hover:text-[var(--text)] transition text-center"
              >
                View Booked Orders (بک شدہ آرڈرز) →
              </button>
            </div>
          </div>
        </div>

        {/* Module 4: Industrial Ledgers Summary */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="p-4 bg-gradient-to-br from-purple-500/20 via-transparent to-transparent border-b border-[var(--steel-line)] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-purple-500 text-white flex items-center justify-center font-bold">
                <Layers size={16} />
              </div>
              <div>
                <h3 className="font-semibold text-sm text-[var(--text)]">Industrial Ledgers</h3>
                <span className="font-serif text-[11px] text-purple-300 font-bold block -mt-0.5">کارخانہ کھاتہ جات</span>
              </div>
            </div>
          </div>

          <div className="p-4 space-y-2 flex-1 font-mono text-xs">
            <div className="flex justify-between items-baseline">
              {renderBilingual('Active Customers (Factories)', 'کسٹمرز (فیکٹریاں)')}
              <span className="font-semibold">{state.customerLedgers.length}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Raw Material Suppliers', 'خام مال سپلائرز')}
              <span className="font-semibold">{state.rawSuppliers.length}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Painters (Colouring)', 'پینٹرز (رنگ والے)')}
              <span className="font-semibold">{state.painters.length}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Labour Workers (Rod Makers)', 'مزدور و کاریگر')}
              <span className="font-semibold">{(state.workers || state.labourWorkers || []).length}</span>
            </div>
            <div className="flex justify-between items-baseline">
              {renderBilingual('Scrap Buyers (Kabaar)', 'سکریپ خریدار')}
              <span className="font-semibold">{state.scrapBuyers.length}</span>
            </div>
            <div className="border-t border-[var(--steel-line)] pt-3">
              <button
                type="button"
                onClick={() => onNavigate('stock')}
                className="w-full py-2 rounded-lg bg-[var(--panel-raised)] hover:border-[var(--yellow)] border border-[var(--steel-line)] text-xs text-[var(--text-dim)] hover:text-[var(--text)] transition text-center"
              >
                Check Stock Levels (اسٹاک چیک کریں) →
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* SECTION: TWO-COLUMN LOWER GRID: LEADERBOARD & RECENT ACTIVITY */}
      {/* ========================================================================= */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Outstanding Receivables Leaderboard */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <div className="flex items-baseline gap-2">
                <h3 className="font-serif font-bold text-base text-[var(--text)]">
                  Who Owes The Most (Receivables)
                </h3>
                <span className="font-serif text-sm font-semibold text-rose-400">واجب الوصول کسٹمرز</span>
              </div>
              <p className="text-[11px] text-[var(--text-dim)] font-mono">
                Top customer outstanding debt across factories
              </p>
            </div>
            <button
              type="button"
              onClick={() => onNavigate('factories')}
              className="text-xs text-[var(--yellow)] hover:underline font-mono"
            >
              All Factories (فیکٹریاں) →
            </button>
          </div>

          <div className="space-y-2.5">
            {debtorFactories.length === 0 ? (
              <p className="text-xs text-[var(--text-dim)] font-mono py-4 text-center">
                All customer accounts are settled! (تمام کسٹمر کھاتے کلیئر ہیں)
              </p>
            ) : (
              debtorFactories.map(f => (
                <div
                  key={f.name}
                  onClick={() => onOpenCustomerLedger(f.name)}
                  className="flex items-center justify-between p-3 rounded-lg bg-[var(--panel-raised)] border border-[var(--steel-line)] hover:border-[var(--yellow)] cursor-pointer transition active:scale-[0.99]"
                >
                  <div>
                    <div className="font-semibold text-sm text-[var(--text)]">{f.name}</div>
                    <div className="text-[11px] text-[var(--text-dim)] font-mono mt-0.5 flex items-center gap-1.5">
                      <span>Tap to view account ledger</span>
                      <span className="font-serif text-amber-300/80">(کھاتہ تفصیل)</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-mono font-bold text-sm text-[var(--red)]">{fmt(f.balance)}</div>
                    <div className="text-[10px] uppercase font-mono tracking-wider text-[var(--text-dim)] flex items-center justify-end gap-1">
                      <span>Owed</span>
                      <span className="font-serif text-rose-400/90">(بقایا)</span>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Recent Activity Log */}
        <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <div className="flex items-baseline gap-2">
                <h3 className="font-serif font-bold text-base text-[var(--text)]">Recent Workshop Activity</h3>
                <span className="font-serif text-sm font-semibold text-amber-300">حالیہ سرگرمیاں</span>
              </div>
              <p className="text-[11px] text-[var(--text-dim)] font-mono">
                Realtime transaction & expenditure logs
              </p>
            </div>
            <span className="text-xs text-[var(--text-dim)] font-mono flex items-center gap-1">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span>Live Timeline</span>
            </span>
          </div>

          <div className="space-y-2.5">
            {recentActivities.map(item => (
              <div
                key={item.id}
                className="flex items-center justify-between p-2.5 rounded-lg bg-[var(--panel-raised)] border border-[var(--steel-line)] font-mono text-xs"
              >
                <div className="min-w-0 flex-1 pr-3">
                  <div className="font-semibold text-[var(--text)] truncate">{item.title}</div>
                  <div className="text-[10px] text-[var(--text-dim)] truncate mt-0.5">{item.sub}</div>
                </div>
                <div
                  className={`font-bold shrink-0 text-sm ${
                    item.positive ? 'text-[var(--green)]' : 'text-[var(--red)]'
                  }`}
                >
                  {item.positive ? '+' : '-'} {fmt(item.amount)}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

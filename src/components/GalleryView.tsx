import React, { useState, useEffect } from 'react';
import {
  Images,
  Package,
  Layers,
  Sparkles,
  Search,
  ShoppingCart,
  Check,
  Eye,
  FileText,
  Printer,
  Sliders,
  Scale,
  Ruler,
  Info,
  ChevronRight,
  Plus,
  Compass,
  Download,
  Trash2,
  ExternalLink,
  FileSpreadsheet,
  Image as ImageIcon,
  Filter,
  Calendar,
  Clock,
  ArrowDownToLine,
  CheckCircle2,
  AlertTriangle,
  Loader2,
  RotateCcw
} from 'lucide-react';
import { Product, AppLanguage, RecipeItem, ExportedItem } from '../types';
import { TRANSLATIONS } from '../utils/i18n';
import { fmt } from '../utils/helpers';
import {
  getExportedItems,
  loadExportedItems,
  deleteExportedItem,
  clearAllExportedItems,
  subscribeToExports,
  exportBlueprintJPG,
  exportSingleTransactionJPG,
  triggerFileDownload
} from '../utils/exportManager';

interface GalleryViewProps {
  products: Product[];
  language: AppLanguage;
  companyName: string;
  initialTab?: 'catalog' | 'custom_designer' | 'exports';
  onAddToCart: (product: Product, color?: string, size?: string) => void;
  onNavigateToPos?: () => void;
}

// Technical fan down rod SVG engineering visualizer component
const FanRodWireframe: React.FC<{
  sizeInches: number;
  colorName?: string;
  isInspecting?: boolean;
  wireGauge?: string;
}> = ({ sizeInches, colorName = 'Matt Black', isInspecting = false, wireGauge = '16 Gauge' }) => {
  // Resolve color palette
  let strokeColor = '#38bdf8';
  let rodFill = 'url(#darkSheen)';
  if (colorName.toLowerCase().includes('black')) {
    strokeColor = colorName.toLowerCase().includes('shine') ? '#475569' : '#334155';
    rodFill = 'url(#darkSheen)';
  } else if (colorName.toLowerCase().includes('white')) {
    strokeColor = '#cbd5e1';
    rodFill = 'url(#whiteSheen)';
  } else if (colorName.toLowerCase().includes('silver') || colorName.toLowerCase().includes('grey')) {
    strokeColor = '#94a3b8';
    rodFill = 'url(#silverSheen)';
  } else if (colorName.toLowerCase().includes('gold') || colorName.toLowerCase().includes('brass')) {
    strokeColor = '#fbbf24';
    rodFill = 'url(#goldSheen)';
  }

  // Visual length scaling clamped to viewBox
  const pipeWidth = Math.min(200, Math.max(120, 110 + (sizeInches - 12) * 2.8));
  const pipeHeight = 22;
  const startX = (260 - pipeWidth) / 2;
  const centerY = 110;

  return (
    <div className={`relative flex items-center justify-center p-3 rounded-xl bg-gradient-to-b from-[#0b1320] to-[#040810] border border-[#1e293b] select-none ${isInspecting ? 'h-64 sm:h-80' : 'h-48'}`}>
      {/* Technical blueprint grid lines */}
      <div className="absolute inset-0 bg-[radial-gradient(#1e293b_1px,transparent_1px)] [background-size:12px_12px] opacity-40 rounded-xl" />

      {/* SVG Fan Down Rod Technical Drawing */}
      <svg
        viewBox="0 0 280 220"
        className="w-full h-full max-h-full drop-shadow-[0_0_15px_rgba(56,189,248,0.15)] transition-transform duration-300 hover:scale-105"
      >
        <defs>
          <linearGradient id="metalGrad" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#38bdf8" />
            <stop offset="40%" stopColor="#0284c7" />
            <stop offset="60%" stopColor="#075985" />
            <stop offset="100%" stopColor="#0369a1" />
          </linearGradient>
          <linearGradient id="darkSheen" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#64748b" />
            <stop offset="35%" stopColor="#1e293b" />
            <stop offset="65%" stopColor="#0f172a" />
            <stop offset="100%" stopColor="#1e293b" />
          </linearGradient>
          <linearGradient id="whiteSheen" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#f8fafc" />
            <stop offset="35%" stopColor="#cbd5e1" />
            <stop offset="70%" stopColor="#94a3b8" />
            <stop offset="100%" stopColor="#e2e8f0" />
          </linearGradient>
          <linearGradient id="silverSheen" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#f1f5f9" />
            <stop offset="35%" stopColor="#94a3b8" />
            <stop offset="65%" stopColor="#64748b" />
            <stop offset="100%" stopColor="#cbd5e1" />
          </linearGradient>
          <linearGradient id="goldSheen" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#fef08a" />
            <stop offset="40%" stopColor="#eab308" />
            <stop offset="70%" stopColor="#a16207" />
            <stop offset="100%" stopColor="#ca8a04" />
          </linearGradient>
        </defs>

        {/* Centerline Construction Wire (Axis) */}
        <line
          x1="20"
          y1={centerY}
          x2="260"
          y2={centerY}
          stroke="#0284c7"
          strokeWidth="0.8"
          strokeDasharray="8 4 2 4"
          className="opacity-40"
        />

        {/* Main Tubular Steel Pipe Body */}
        <rect
          x={startX}
          y={centerY - pipeHeight / 2}
          width={pipeWidth}
          height={pipeHeight}
          rx="3"
          fill={rodFill}
          stroke={strokeColor}
          strokeWidth="1.8"
          className="opacity-95"
        />

        {/* Internal Wiring Conduit Channel (Dashed) */}
        <line
          x1={startX + 14}
          y1={centerY - 4}
          x2={startX + pipeWidth - 14}
          y2={centerY - 4}
          stroke="#38bdf8"
          strokeWidth="0.8"
          strokeDasharray="3 2"
          className="opacity-60"
        />
        <line
          x1={startX + 14}
          y1={centerY + 4}
          x2={startX + pipeWidth - 14}
          y2={centerY + 4}
          stroke="#38bdf8"
          strokeWidth="0.8"
          strokeDasharray="3 2"
          className="opacity-60"
        />

        {/* Left Side: Ceiling Shackle Collar / Bracket Fitting */}
        <rect
          x={startX - 10}
          y={centerY - 15}
          width="12"
          height="30"
          rx="2"
          fill="#1e293b"
          stroke="#f59e0b"
          strokeWidth="1.8"
        />
        {/* Shackle Cross-Bolt Hole */}
        <circle cx={startX - 4} cy={centerY} r="3.5" fill="#0b1320" stroke="#f59e0b" strokeWidth="1.2" />
        <line x1={startX - 4} y1={centerY - 5.5} x2={startX - 4} y2={centerY + 5.5} stroke="#38bdf8" strokeWidth="0.8" />

        {/* Safety Cotter Pin Slit */}
        <line x1={startX + 4} y1={centerY - 10} x2={startX + 4} y2={centerY + 10} stroke="#f59e0b" strokeWidth="1.2" strokeLinecap="round" />

        {/* Upper Canopy Seating Ring (Rubber Grommet) */}
        <rect
          x={startX + 22}
          y={centerY - 13}
          width="5"
          height="26"
          rx="1.5"
          fill="#334155"
          stroke="#64748b"
          strokeWidth="1"
        />

        {/* Lower Canopy Retainer Step Ring */}
        <rect
          x={startX + pipeWidth - 28}
          y={centerY - 13}
          width="5"
          height="26"
          rx="1.5"
          fill="#334155"
          stroke="#64748b"
          strokeWidth="1"
        />

        {/* Right Side: Fan Motor Spindle Coupler Collar */}
        <rect
          x={startX + pipeWidth - 2}
          y={centerY - 15}
          width="14"
          height="30"
          rx="2"
          fill="#1e293b"
          stroke="#f59e0b"
          strokeWidth="1.8"
        />
        {/* Motor Shaft Lock-Bolt Through-Holes */}
        <circle cx={startX + pipeWidth + 5} cy={centerY - 5} r="2.8" fill="#0b1320" stroke="#f59e0b" strokeWidth="1.2" />
        <circle cx={startX + pipeWidth + 5} cy={centerY + 5} r="2.8" fill="#0b1320" stroke="#f59e0b" strokeWidth="1.2" />

        {/* Falcon Logo Insignia on Rod Body */}
        <image
          href="/falcon-logo.png"
          x={126}
          y={centerY - 8}
          width="24"
          height="16"
          className="select-none opacity-85"
        />

        {/* CAD Dimension Callouts */}
        <line x1={startX - 10} y1={centerY + 34} x2={startX + pipeWidth + 12} y2={centerY + 34} stroke="#f59e0b" strokeWidth="1.2" />
        <line x1={startX - 10} y1={centerY + 26} x2={startX - 10} y2={centerY + 42} stroke="#f59e0b" strokeWidth="1.2" />
        <line x1={startX + pipeWidth + 12} y1={centerY + 26} x2={startX + pipeWidth + 12} y2={centerY + 42} stroke="#f59e0b" strokeWidth="1.2" />
        <text x="140" y={centerY + 52} textAnchor="middle" fill="#f59e0b" fontSize="9.5" fontFamily="monospace" fontWeight="bold">
          L = {sizeInches}&quot; ({Math.round(sizeInches * 25.4)} mm)
        </text>

        {isInspecting && (
          <>
            {/* Outer Diameter Measurement */}
            <line x1={startX - 24} y1={centerY - 11} x2={startX - 24} y2={centerY + 11} stroke="#38bdf8" strokeWidth="1" />
            <line x1={startX - 28} y1={centerY - 11} x2={startX - 20} y2={centerY - 11} stroke="#38bdf8" strokeWidth="1" />
            <line x1={startX - 28} y1={centerY + 11} x2={startX - 20} y2={centerY + 11} stroke="#38bdf8" strokeWidth="1" />
            <text x={startX - 30} y={centerY + 3} textAnchor="end" fill="#38bdf8" fontSize="8" fontFamily="monospace">
              Ø 3/4&quot;
            </text>
            <text x="140" y="32" textAnchor="middle" fill="#94a3b8" fontSize="8.5" fontFamily="monospace">
              M.S. STEEL TUBING • {wireGauge} • SAFETY PIN SPEC
            </text>
          </>
        )}
      </svg>

      {/* Floating Specs Pill */}
      <div className="absolute top-2.5 left-2.5 flex items-center gap-1.5 px-2 py-0.5 rounded bg-black/60 backdrop-blur-md border border-white/10 font-mono text-[10px] text-zinc-300">
        <span className="w-2 h-2 rounded-full" style={{ backgroundColor: strokeColor }} />
        <span>{sizeInches}&quot; Length</span>
      </div>

      <div className="absolute bottom-2.5 right-2.5 px-2 py-0.5 rounded bg-black/60 backdrop-blur-md border border-white/10 font-mono text-[9px] text-amber-400 font-bold uppercase">
        {wireGauge} Pipe
      </div>
    </div>
  );
};

export const GalleryView: React.FC<GalleryViewProps> = ({
  products,
  language,
  companyName,
  initialTab = 'catalog',
  onAddToCart,
  onNavigateToPos
}) => {
  const [selectedCat, setSelectedCat] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [cartFeedback, setCartFeedback] = useState<number | null>(null);
  const [selectedColors, setSelectedColors] = useState<Record<number, string>>({});
  const [selectedSizes, setSelectedSizes] = useState<Record<number, string>>({});
  const [activeTab, setActiveTab] = useState<'catalog' | 'custom_designer' | 'exports'>(initialTab);
  const [customCartAdded, setCustomCartAdded] = useState(false);

  React.useEffect(() => {
    if (initialTab) {
      setActiveTab(initialTab);
    }
  }, [initialTab]);

  // Gallery Exports Archive State
  const [exportedItems, setExportedItems] = useState<ExportedItem[]>([]);
  const [exportFilterFormat, setExportFilterFormat] = useState<'all' | 'jpg' | 'pdf' | 'csv'>('all');
  const [exportFilterCategory, setExportFilterCategory] = useState<'all' | 'invoice' | 'report' | 'blueprint' | 'receipt'>('all');
  const [exportSearch, setExportSearch] = useState('');
  const [previewItem, setPreviewItem] = useState<ExportedItem | null>(null);
  const [itemToDelete, setItemToDelete] = useState<ExportedItem | null>(null);
  const [showClearAllConfirm, setShowClearAllConfirm] = useState(false);
  const [isExportingBlueprint, setIsExportingBlueprint] = useState(false);
  const [blueprintSuccess, setBlueprintSuccess] = useState(false);
  const [isGeneratingSampleJpg, setIsGeneratingSampleJpg] = useState(false);
  const [imageErrors, setImageErrors] = useState<Record<string, boolean>>({});

  const handleGenerateSampleJpg = async () => {
    try {
      setIsGeneratingSampleJpg(true);
      const demoTxn = {
        id: `TXN-${Math.floor(1000 + Math.random() * 9000)}`,
        date: new Date().toISOString().split('T')[0],
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        factory: 'Royal Fan Works (Gujrat)',
        itemsSummary: '24" Heavy Duty Fan Rod (Matt Black) x 100 pcs\n18" Standard Fan Rod (Moon White) x 50 pcs',
        itemCount: 150,
        total: 185000,
        paid: true,
        confirmed: true,
        method: 'Bank Transfer / Meezan'
      };
      await exportSingleTransactionJPG({
        transaction: demoTxn,
        customerPayments: [],
        companyName
      });
      // Ensure exports filter is showing all or jpg
      if (exportFilterFormat !== 'all' && exportFilterFormat !== 'jpg') {
        setExportFilterFormat('all');
      }
    } catch (err) {
      console.error('Failed to generate sample JPG invoice', err);
    } finally {
      setIsGeneratingSampleJpg(false);
    }
  };

  // Keep exports in sync with IndexedDB, memory cache, and global export events
  useEffect(() => {
    // 1. Instant synchronous cache retrieval
    setExportedItems(getExportedItems());

    // 2. Hydrate full high-resolution data URLs from IndexedDB
    loadExportedItems().then(items => {
      if (items && items.length > 0) {
        setExportedItems(items);
      }
    });

    // 3. Listen for ongoing export recordings
    const unsubscribe = subscribeToExports(items => {
      setExportedItems(items);
      setPreviewItem(currentPreview => {
        if (!currentPreview) return null;
        const matching = items.find(i => i.id === currentPreview.id);
        return matching || currentPreview;
      });
    });
    return () => unsubscribe();
  }, []);

  // Custom Fan Rod Designer State
  const [calcDiameter, setCalcDiameter] = useState<number>(18);
  const [calcSpokes, setCalcSpokes] = useState<number>(12);
  const [calcGauge, setCalcGauge] = useState<string>('16 Gauge');
  const [calcColor, setCalcColor] = useState<string>('Matt Black');
  const [calcHasMono, setCalcHasMono] = useState<boolean>(true);

  const t = (key: string) => TRANSLATIONS[language]?.[key] || TRANSLATIONS.en[key] || key;

  // Filtered exports list
  const filteredExports = exportedItems.filter(item => {
    const matchFormat = exportFilterFormat === 'all' || item.format.toLowerCase() === exportFilterFormat.toLowerCase();
    const matchCategory = exportFilterCategory === 'all' || item.category.toLowerCase() === exportFilterCategory.toLowerCase();
    const q = exportSearch.trim().toLowerCase();
    const matchSearch =
      !q ||
      item.title.toLowerCase().includes(q) ||
      item.fileName.toLowerCase().includes(q) ||
      (item.customerName && item.customerName.toLowerCase().includes(q)) ||
      (item.description && item.description.toLowerCase().includes(q));
    return matchFormat && matchCategory && matchSearch;
  });

  const handleExportCustomBlueprintJPG = async () => {
    try {
      setIsExportingBlueprint(true);
      await exportBlueprintJPG({
        diameter: calcDiameter,
        spokes: calcSpokes,
        gauge: calcGauge,
        color: calcColor,
        hasMono: calcHasMono,
        weightKg: totalEstimatedWeightKg,
        price: estimatedWholesalePrice,
        companyName
      });
      setBlueprintSuccess(true);
      setTimeout(() => setBlueprintSuccess(false), 2500);
    } catch (err) {
      console.error('Error generating blueprint JPG', err);
    } finally {
      setIsExportingBlueprint(false);
    }
  };

  const handleExportProductBlueprintJPG = async (prod: Product) => {
    try {
      setIsExportingBlueprint(true);
      const sizeVal = parseInt(prod.size || '18', 10) || 18;
      const weightVal = parseFloat(prod.weight || '1.2') || 1.2;
      await exportBlueprintJPG({
        diameter: sizeVal,
        spokes: sizeVal >= 24 ? 16 : sizeVal >= 18 ? 12 : 8,
        gauge: '12 Gauge',
        color: prod.color || 'Black',
        hasMono: true,
        weightKg: weightVal,
        price: prod.price,
        companyName
      });
      setBlueprintSuccess(true);
      setTimeout(() => setBlueprintSuccess(false), 2500);
    } catch (err) {
      console.error('Error generating product blueprint JPG', err);
    } finally {
      setIsExportingBlueprint(false);
    }
  };

  const handleDeleteConfirmed = () => {
    if (itemToDelete) {
      deleteExportedItem(itemToDelete.id);
      if (previewItem?.id === itemToDelete.id) {
        setPreviewItem(null);
      }
      setItemToDelete(null);
    }
  };

  const handleClearAllConfirmed = () => {
    clearAllExportedItems();
    setPreviewItem(null);
    setShowClearAllConfirm(false);
  };

  const handleDownloadItem = (item: ExportedItem) => {
    if (item.dataUrl) {
      triggerFileDownload(item.dataUrl, item.fileName);
    }
  };

  // Categories extracted from products
  const categories = ['All', ...Array.from(new Set(products.map(p => p.cat).filter(Boolean)))];

  const filteredProducts = products.filter(p => {
    const matchCat = selectedCat === 'All' || p.cat === selectedCat;
    const matchQuery =
      searchQuery === '' ||
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.cat && p.cat.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (p.color && p.color.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchCat && matchQuery;
  });

  const handleAddWithFeedback = (product: Product) => {
    const chosenColor = selectedColors[product.id] || product.color;
    const chosenSize = selectedSizes[product.id] || product.size;
    onAddToCart(product, chosenColor, chosenSize);
    setCartFeedback(product.id);
    setTimeout(() => setCartFeedback(null), 1500);
  };

  // Calculations for custom fan rod designer
  const estimatedPipeWeightKg = Math.round((calcDiameter * (calcGauge === '14 Gauge' ? 0.052 : calcGauge === '16 Gauge' ? 0.042 : 0.035)) * 100) / 100;
  const estimatedFittingsWeightKg = 0.06;
  const totalEstimatedWeightKg = Math.round((estimatedPipeWeightKg + estimatedFittingsWeightKg) * 100) / 100;
  const estimatedWholesalePrice = Math.round((totalEstimatedWeightKg * 290) + 45);

  const handlePrintSpecSheet = () => {
    window.print();
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto font-mono text-[var(--text)]">
      {/* Top Banner Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--steel-line)] pb-4 font-sans">
        <div className="flex items-start sm:items-center gap-3.5">
          <div className="bg-white rounded-xl p-1.5 px-2.5 shadow-xs border border-slate-200/80 shrink-0 hidden sm:flex items-center justify-center">
            <img
              src="/falcon-logo.png"
              alt="Falcon Rod Maker"
              className="h-10 w-auto object-contain select-none animate-logo-glow"
            />
          </div>
          <div>
            <div className="flex items-center gap-2 text-[var(--yellow)] font-mono text-xs font-semibold uppercase tracking-wider mb-1">
              <Images size={16} />
              <span>Industrial Technical Showcase</span>
            </div>
            <h2 className="text-xl sm:text-2xl font-serif font-black tracking-tight text-[var(--text)]">
              Fan Rod Blueprint Gallery & Visual Catalog
            </h2>
            <p className="text-xs text-[var(--text-dim)] font-mono mt-0.5">
              Precision engineering specifications, tubular CAD wireframes, bills of materials, and manufacturing blueprints.
            </p>
          </div>
        </div>

        {/* View Toggle (Catalog vs Custom Rod Designer) */}
        <div className="flex items-center bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-xl p-1 gap-1 self-start sm:self-auto">
          <button
            type="button"
            onClick={() => setActiveTab('catalog')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition ${
              activeTab === 'catalog'
                ? 'bg-[var(--yellow)] text-black shadow-sm'
                : 'text-[var(--text-dim)] hover:text-[var(--text)]'
            }`}
          >
            <Package size={14} />
            <span>Finished Catalog</span>
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('custom_designer')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition ${
              activeTab === 'custom_designer'
                ? 'bg-[var(--yellow)] text-black shadow-sm'
                : 'text-[var(--text-dim)] hover:text-[var(--text)]'
            }`}
          >
            <Compass size={14} />
            <span>Blueprint Designer</span>
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('exports')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition relative ${
              activeTab === 'exports'
                ? 'bg-[var(--yellow)] text-black shadow-sm'
                : 'text-[var(--text-dim)] hover:text-[var(--text)]'
            }`}
          >
            <ImageIcon size={14} />
            <span>Export Gallery</span>
            {exportedItems.length > 0 && (
              <span
                className={`px-1.5 py-0.2 rounded-full text-[10px] font-bold ${
                  activeTab === 'exports'
                    ? 'bg-black text-[var(--yellow)]'
                    : 'bg-[var(--yellow)]/20 text-[var(--yellow)] border border-[var(--yellow)]/30'
                }`}
              >
                {exportedItems.length}
              </span>
            )}
          </button>
        </div>
      </div>

      {activeTab === 'catalog' && (
        <>
          {/* Controls bar: Search and Filter Pills */}
          <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3 bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-3">
            {/* Search Input */}
            <div className="relative flex-1 min-w-[200px]">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-dim)]" />
              <input
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                placeholder="Search fan rods by name, size, or style..."
                className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg pl-9 pr-3 py-1.5 text-xs text-[var(--text)] placeholder-[var(--text-dim)] focus:outline-none focus:border-[var(--yellow)]"
              />
            </div>

            {/* Category Filter Chips */}
            <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0 scrollbar-none">
              {categories.map(cat => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setSelectedCat(cat)}
                  className={`px-2.5 py-1 rounded-lg text-[11px] font-mono whitespace-nowrap transition ${
                    selectedCat === cat
                      ? 'bg-[var(--yellow)] text-black font-bold shadow'
                      : 'bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)]'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          {/* Product Cards Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredProducts.map(product => {
              const sizeVal = parseInt(product.size || '18', 10) || 18;
              const activeColor = selectedColors[product.id] || product.color || 'Black';
              const isAdded = cartFeedback === product.id;

              return (
                <div
                  key={product.id}
                  className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl p-4 flex flex-col justify-between group hover:border-[var(--yellow)]/60 transition-all duration-200 shadow-sm"
                >
                  <div>
                    {/* Visual Wireframe Preview */}
                    <FanRodWireframe
                      sizeInches={sizeVal}
                      colorName={activeColor}
                      wireGauge={product.gauge || '16 Gauge'}
                    />

                    {/* Metadata details */}
                    <div className="mt-3.5 space-y-2">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <span className="text-[10px] font-mono font-semibold uppercase text-[var(--yellow)]">
                            {product.cat}
                          </span>
                          <h3 className="font-serif font-bold text-sm sm:text-base text-[var(--text)] group-hover:text-[var(--yellow)] transition-colors line-clamp-1">
                            {product.name}
                          </h3>
                        </div>
                        <div className="text-right shrink-0">
                          <span className="font-mono font-bold text-sm sm:text-base text-emerald-400">
                            Rs {fmt(product.price)}
                          </span>
                          <span className="block text-[9px] text-[var(--text-dim)]">per piece</span>
                        </div>
                      </div>

                      {/* Specs Row */}
                      <div className="grid grid-cols-3 gap-2 py-2 border-y border-[var(--steel-line)] text-center text-[10px]">
                        <div>
                          <span className="text-[var(--text-dim)] block">Length</span>
                          <span className="font-bold text-[var(--text)]">{product.size || '18"'}</span>
                        </div>
                        <div>
                          <span className="text-[var(--text-dim)] block">Weight</span>
                          <span className="font-bold text-[var(--text)]">{product.weight || '1.2 kg'}</span>
                        </div>
                        <div>
                          <span className="text-[var(--text-dim)] block">Stock</span>
                          <span className={`font-bold ${(product.stock || 0) <= (product.reorderLevel || 10) ? 'text-red-400' : 'text-[var(--text)]'}`}>
                            {product.stock || 0} pcs
                          </span>
                        </div>
                      </div>

                      {/* Color Option Selector */}
                      <div className="flex items-center justify-between pt-1 text-[11px]">
                        <span className="text-[var(--text-dim)] text-[10px]">Color / Finish:</span>
                        <div className="flex items-center gap-1">
                          {['Shine Black', 'Matt Black', 'White', 'Silver'].map(clr => (
                            <button
                              key={clr}
                              type="button"
                              onClick={() => setSelectedColors(prev => ({ ...prev, [product.id]: clr }))}
                              title={clr}
                              className={`px-1.5 py-0.5 text-[9px] rounded border transition ${
                                activeColor === clr
                                  ? 'border-[var(--yellow)] bg-[var(--yellow)]/15 text-[var(--yellow)] font-bold'
                                  : 'border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)]'
                              }`}
                            >
                              {clr.split(' ')[0]}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Actions Footer */}
                  <div className="mt-4 pt-3 border-t border-[var(--steel-line)] flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setSelectedProduct(product)}
                      className="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-[var(--panel-raised)] border border-[var(--steel-line)] text-xs font-semibold text-[var(--text)] hover:border-[var(--yellow)] transition active:scale-95"
                    >
                      <Eye size={13} className="text-[var(--yellow)]" />
                      <span>Blueprint</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => handleAddWithFeedback(product)}
                      className={`flex-1 flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold transition active:scale-95 shadow-sm ${
                        isAdded
                          ? 'bg-emerald-500 text-white'
                          : 'bg-[var(--yellow)] text-black hover:brightness-110'
                      }`}
                    >
                      {isAdded ? <Check size={13} /> : <ShoppingCart size={13} />}
                      <span>{isAdded ? 'Added to Cart' : 'Order POS'}</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

          {filteredProducts.length === 0 && (
            <div className="text-center py-12 border border-dashed border-[var(--steel-line)] rounded-2xl p-6">
              <Images size={36} className="mx-auto text-[var(--text-dim)] mb-2 opacity-50" />
              <p className="text-sm font-semibold text-[var(--text)]">No Fan Rods Found</p>
              <p className="text-xs text-[var(--text-dim)] mt-1">Try selecting another category or clear the search query.</p>
            </div>
          )}
        </>
      )}

      {/* Custom Blueprint Fan Rod Designer / Estimator */}
      {activeTab === 'custom_designer' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl p-5 sm:p-7">
          {/* Controls Left Column */}
          <div className="lg:col-span-6 space-y-5">
            <div>
              <span className="text-[10px] font-mono uppercase tracking-wider text-[var(--yellow)] font-bold">
                Workshop Estimator
              </span>
              <h3 className="font-serif font-black text-lg sm:text-xl text-[var(--text)]">
                Custom Fan Rod Specification Calculator
              </h3>
              <p className="text-xs text-[var(--text-dim)] mt-1">
                Configure dimensional parameters to compute exact steel pipe weight, safety fittings, and manufacturing costs.
              </p>
            </div>

            {/* Slider: Rod Length */}
            <div className="space-y-1.5 bg-[var(--panel-raised)] p-3.5 rounded-xl border border-[var(--steel-line)]">
              <div className="flex items-center justify-between text-xs">
                <span className="text-[var(--text-dim)]">Rod Length (Inches):</span>
                <span className="font-bold text-[var(--yellow)] text-sm">{calcDiameter}&quot;</span>
              </div>
              <input
                type="range"
                min="12"
                max="48"
                step="6"
                value={calcDiameter}
                onChange={e => setCalcDiameter(parseInt(e.target.value, 10))}
                className="w-full accent-[var(--yellow)] cursor-pointer"
              />
              <div className="flex justify-between text-[9px] text-[var(--text-dim)] font-mono">
                <span>12&quot; (Standard)</span>
                <span>18&quot; (Popular)</span>
                <span>24&quot; (Deep)</span>
                <span>36&quot; (High Ceiling)</span>
                <span>48&quot; (Industrial)</span>
              </div>
            </div>

            {/* Pipe Gauge & Finish Selectors */}
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-[10px] uppercase text-[var(--text-dim)]">Pipe Wall Gauge</label>
                <select
                  value={calcGauge}
                  onChange={e => setCalcGauge(e.target.value)}
                  className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg px-2.5 py-2 text-xs text-[var(--text)] focus:outline-none focus:border-[var(--yellow)]"
                >
                  <option value="14 Gauge">14 Gauge (Heavy Industrial)</option>
                  <option value="16 Gauge">16 Gauge (Standard Heavy)</option>
                  <option value="18 Gauge">18 Gauge (Light Commercial)</option>
                </select>
              </div>

              <div className="space-y-1">
                <label className="text-[10px] uppercase text-[var(--text-dim)]">Surface Coating</label>
                <select
                  value={calcColor}
                  onChange={e => setCalcColor(e.target.value)}
                  className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-lg px-2.5 py-2 text-xs text-[var(--text)] focus:outline-none focus:border-[var(--yellow)]"
                >
                  <option value="Matt Black">Matt Black Powder</option>
                  <option value="Pure White">Pure White Gloss</option>
                  <option value="Smoke Grey">Smoke Grey Metallic</option>
                  <option value="Precision Silver">Precision Silver Metallic</option>
                  <option value="Antique Gold">Antique Gold Brass</option>
                </select>
              </div>
            </div>

            {/* Fittings checklist */}
            <div className="flex items-center gap-4 text-xs">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={calcHasMono}
                  onChange={e => setCalcHasMono(e.target.checked)}
                  className="rounded accent-[var(--yellow)]"
                />
                <span>Include Pre-Drilled Safety Bolt & Cotter Pin Holes</span>
              </label>
            </div>
          </div>

          {/* Blueprint Visual & Calculated Bill of Materials Right Column */}
          <div className="lg:col-span-6 flex flex-col justify-between space-y-4">
            <FanRodWireframe
              sizeInches={calcDiameter}
              colorName={calcColor}
              isInspecting={true}
              wireGauge={calcGauge}
            />

            {/* Engineered Recipe Output Box */}
            <div className="bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-xl p-4 space-y-3">
              <h4 className="font-serif font-bold text-xs uppercase text-[var(--yellow)] flex items-center gap-1.5">
                <Scale size={14} />
                <span>Calculated Material Consumption</span>
              </h4>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-center text-xs">
                <div className="p-2 rounded bg-black/30 border border-white/5">
                  <span className="text-[9px] text-[var(--text-dim)] block">Steel Pipe</span>
                  <span className="font-bold text-[var(--text)]">{estimatedPipeWeightKg} kg</span>
                </div>
                <div className="p-2 rounded bg-black/30 border border-white/5">
                  <span className="text-[9px] text-[var(--text-dim)] block">Fittings & Pin</span>
                  <span className="font-bold text-[var(--text)]">{estimatedFittingsWeightKg} kg</span>
                </div>
                <div className="p-2 rounded bg-black/30 border border-white/5">
                  <span className="text-[9px] text-[var(--text-dim)] block">Net Weight</span>
                  <span className="font-bold text-amber-400">{totalEstimatedWeightKg} kg</span>
                </div>
                <div className="p-2 rounded bg-black/30 border border-white/5">
                  <span className="text-[9px] text-[var(--text-dim)] block">Est. Rate</span>
                  <span className="font-bold text-emerald-400">Rs {fmt(estimatedWholesalePrice)}</span>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="pt-2 space-y-2">
                <button
                  type="button"
                  onClick={() => {
                    const customItem: Product = {
                      id: Date.now(),
                      name: `Custom-${calcDiameter}in-${calcColor.replace(/\s+/g, '-')}`,
                      price: estimatedWholesalePrice,
                      cat: `${calcDiameter}" Custom Specification`,
                      color: calcColor,
                      size: `${calcDiameter} inch`,
                      weight: `${totalEstimatedWeightKg} kg`,
                      stock: 1,
                      reorderLevel: 5
                    };
                    onAddToCart(customItem, calcColor, `${calcDiameter} inch`);
                    setCustomCartAdded(true);
                    setTimeout(() => setCustomCartAdded(false), 2200);
                  }}
                  className={`w-full flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-xs shadow transition active:scale-95 ${
                    customCartAdded
                      ? 'bg-emerald-500 text-white shadow-emerald-500/20'
                      : 'bg-[var(--yellow)] text-black hover:brightness-110'
                  }`}
                >
                  {customCartAdded ? (
                    <>
                      <Check size={14} className="stroke-[3]" />
                      <span>Added to POS Invoice Cart!</span>
                    </>
                  ) : (
                    <>
                      <ShoppingCart size={14} />
                      <span>Send Spec Rod Directly to POS Cart</span>
                    </>
                  )}
                </button>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={handleExportCustomBlueprintJPG}
                    disabled={isExportingBlueprint}
                    className="flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400 font-bold text-xs hover:bg-amber-500/20 active:scale-95 transition disabled:opacity-50"
                  >
                    {isExportingBlueprint ? (
                      <>
                        <Loader2 size={14} className="animate-spin" />
                        <span>Exporting Blueprint JPG...</span>
                      </>
                    ) : blueprintSuccess ? (
                      <>
                        <CheckCircle2 size={14} className="text-emerald-400" />
                        <span className="text-emerald-400">Saved to Gallery & Downloaded!</span>
                      </>
                    ) : (
                      <>
                        <ImageIcon size={14} />
                        <span>Export Blueprint (JPG)</span>
                      </>
                    )}
                  </button>

                  <button
                    type="button"
                    onClick={handlePrintSpecSheet}
                    className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)] text-xs font-bold transition"
                  >
                    <Printer size={14} />
                    <span>Print</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Export Gallery & Archive Tab */}
      {activeTab === 'exports' && (
        <div className="space-y-6">
          {/* Summary Statistics Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 font-mono">
            <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-3.5 flex items-center justify-between">
              <div>
                <span className="text-[10px] uppercase text-[var(--text-dim)] font-bold block">Total Exports</span>
                <span className="text-xl sm:text-2xl font-black text-[var(--text)]">{exportedItems.length}</span>
              </div>
              <div className="w-10 h-10 rounded-xl bg-[var(--panel-raised)] border border-[var(--steel-line)] flex items-center justify-center text-[var(--yellow)]">
                <Download size={20} />
              </div>
            </div>

            <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-3.5 flex items-center justify-between">
              <div>
                <span className="text-[10px] uppercase text-amber-400/90 font-bold block">JPG Images</span>
                <span className="text-xl sm:text-2xl font-black text-amber-400">
                  {exportedItems.filter(i => i.format === 'jpg').length}
                </span>
              </div>
              <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400">
                <ImageIcon size={20} />
              </div>
            </div>

            <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-3.5 flex items-center justify-between">
              <div>
                <span className="text-[10px] uppercase text-rose-400/90 font-bold block">PDF Documents</span>
                <span className="text-xl sm:text-2xl font-black text-rose-400">
                  {exportedItems.filter(i => i.format === 'pdf').length}
                </span>
              </div>
              <div className="w-10 h-10 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400">
                <FileText size={20} />
              </div>
            </div>

            <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-xl p-3.5 flex items-center justify-between">
              <div>
                <span className="text-[10px] uppercase text-emerald-400/90 font-bold block">CSV Sheets</span>
                <span className="text-xl sm:text-2xl font-black text-emerald-400">
                  {exportedItems.filter(i => i.format === 'csv').length}
                </span>
              </div>
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                <FileSpreadsheet size={20} />
              </div>
            </div>
          </div>

          {/* Filter & Search Bar */}
          <div className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl p-4 space-y-3 font-sans">
            <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
              {/* Search */}
              <div className="relative flex-1 min-w-[240px]">
                <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-dim)]" />
                <input
                  type="text"
                  value={exportSearch}
                  onChange={e => setExportSearch(e.target.value)}
                  placeholder="Search exported files by title, filename, customer..."
                  className="w-full bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-xl pl-9 pr-8 py-2 text-xs text-[var(--text)] placeholder-[var(--text-dim)] focus:outline-none focus:border-[var(--yellow)] font-mono"
                />
                {exportSearch && (
                  <button
                    onClick={() => setExportSearch('')}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs text-[var(--text-dim)] hover:text-[var(--text)]"
                  >
                    ✕
                  </button>
                )}
              </div>

              {/* Header Actions */}
              <div className="flex items-center gap-2 self-end md:self-auto flex-wrap">
                <button
                  type="button"
                  disabled={isGeneratingSampleJpg}
                  onClick={handleGenerateSampleJpg}
                  className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-amber-500/15 border border-amber-500/40 text-amber-400 hover:bg-amber-500 hover:text-black text-xs font-mono font-bold transition cursor-pointer active:scale-95 disabled:opacity-50"
                  title="Generate high-resolution sample JPG invoice to test export and gallery rendering"
                >
                  {isGeneratingSampleJpg ? (
                    <>
                      <Loader2 size={13} className="animate-spin" />
                      <span>Exporting JPG...</span>
                    </>
                  ) : (
                    <>
                      <ImageIcon size={13} />
                      <span>+ Sample JPG Invoice</span>
                    </>
                  )}
                </button>

                {/* Clear All Exports Button */}
                {exportedItems.length > 0 && (
                  <button
                    type="button"
                    onClick={() => setShowClearAllConfirm(true)}
                    className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-red-500/10 border border-red-500/25 text-red-400 hover:bg-red-500/20 text-xs font-mono font-bold transition"
                  >
                    <Trash2 size={13} />
                    <span>Clear All Exports</span>
                  </button>
                )}
              </div>
            </div>

            {/* Filter Row */}
            <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-[var(--steel-line)]/50 text-xs font-mono">
              {/* Format Filters */}
              <div className="flex items-center gap-1.5 flex-wrap">
                <span className="text-[11px] text-[var(--text-dim)] mr-1 flex items-center gap-1">
                  <Filter size={12} /> Format:
                </span>
                {(['all', 'jpg', 'pdf', 'csv'] as const).map(fmt => (
                  <button
                    key={fmt}
                    type="button"
                    onClick={() => setExportFilterFormat(fmt)}
                    className={`px-2.5 py-1 rounded-lg text-[11px] font-bold uppercase transition ${
                      exportFilterFormat === fmt
                        ? fmt === 'jpg'
                          ? 'bg-amber-500 text-black shadow'
                          : fmt === 'pdf'
                          ? 'bg-rose-500 text-white shadow'
                          : fmt === 'csv'
                          ? 'bg-emerald-500 text-white shadow'
                          : 'bg-[var(--yellow)] text-black shadow'
                        : 'bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)]'
                    }`}
                  >
                    {fmt === 'all' ? 'All Formats' : fmt.toUpperCase()}
                  </button>
                ))}
              </div>

              {/* Category Filters */}
              <div className="flex items-center gap-1.5 flex-wrap">
                <span className="text-[11px] text-[var(--text-dim)] mr-1">Type:</span>
                {(['all', 'invoice', 'report', 'blueprint', 'receipt'] as const).map(cat => (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => setExportFilterCategory(cat)}
                    className={`px-2.5 py-1 rounded-lg text-[11px] transition ${
                      exportFilterCategory === cat
                        ? 'bg-[var(--yellow)] text-black font-bold shadow'
                        : 'bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[var(--text-dim)] hover:text-[var(--text)]'
                    }`}
                  >
                    {cat === 'all'
                      ? 'All Categories'
                      : cat === 'invoice'
                      ? 'Invoices'
                      : cat === 'report'
                      ? 'Reports'
                      : cat === 'blueprint'
                      ? 'Blueprints'
                      : 'Receipts'}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Exported Items Grid */}
          {filteredExports.length === 0 ? (
            <div className="text-center py-16 border-2 border-dashed border-[var(--steel-line)] rounded-2xl p-8 bg-[var(--panel)]">
              <div className="w-16 h-16 rounded-2xl bg-[var(--panel-raised)] border border-[var(--steel-line)] mx-auto flex items-center justify-center text-[var(--text-dim)] mb-4">
                <ImageIcon size={32} />
              </div>
              <h3 className="font-serif font-bold text-lg text-[var(--text)]">No Exported Files Found</h3>
              <p className="text-xs text-[var(--text-dim)] max-w-md mx-auto mt-2 font-mono">
                {exportedItems.length === 0
                  ? 'All invoices, reports, blueprints, and ledgers exported in JPG, PDF, or CSV format are automatically archived here with instant preview, re-download, and deletion capabilities.'
                  : 'No exported files match your current search or format filters.'}
              </p>
              {exportedItems.length === 0 && (
                <div className="mt-5 flex justify-center">
                  <button
                    type="button"
                    disabled={isGeneratingSampleJpg}
                    onClick={handleGenerateSampleJpg}
                    className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[var(--yellow)] text-black font-mono font-bold text-xs uppercase shadow hover:bg-amber-400 transition cursor-pointer active:scale-95 disabled:opacity-50"
                  >
                    {isGeneratingSampleJpg ? (
                      <>
                        <Loader2 size={14} className="animate-spin" />
                        <span>Rendering High-Res JPG...</span>
                      </>
                    ) : (
                      <>
                        <ImageIcon size={14} />
                        <span>Generate Sample Invoice (JPG)</span>
                      </>
                    )}
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 font-sans">
              {filteredExports.map(item => {
                const isJpg = item.format === 'jpg';
                const isPdf = item.format === 'pdf';
                const isCsv = item.format === 'csv';

                return (
                  <div
                    key={item.id}
                    className="bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl overflow-hidden shadow-sm hover:border-[var(--yellow)]/60 transition group flex flex-col justify-between"
                  >
                    <div>
                      {/* Visual Preview Banner */}
                      <div
                        onClick={() => setPreviewItem(item)}
                        className="relative h-44 w-full bg-[#070b12] border-b border-[var(--steel-line)] overflow-hidden cursor-pointer flex items-center justify-center group-hover:opacity-95 transition"
                      >
                        {isJpg ? (
                          item.dataUrl && !imageErrors[item.id] ? (
                            <img
                              src={item.dataUrl}
                              alt={item.title}
                              onError={() => setImageErrors(prev => ({ ...prev, [item.id]: true }))}
                              className="w-full h-full object-cover object-top transition duration-300 group-hover:scale-105"
                              loading="lazy"
                            />
                          ) : (
                            <div className="flex flex-col items-center justify-center gap-2 p-4 text-center bg-gradient-to-b from-[#181a20] to-[#0f1115] w-full h-full">
                              <div className="w-14 h-14 rounded-2xl bg-amber-500/15 border border-amber-500/35 flex items-center justify-center text-amber-400 shadow">
                                <ImageIcon size={28} />
                              </div>
                              <div className="space-y-0.5">
                                <span className="font-mono text-[11px] text-amber-300 uppercase tracking-wider font-bold block">
                                  High-Resolution JPG
                                </span>
                                <span className="text-[10px] text-[var(--text-dim)] font-mono block">
                                  {item.category === 'invoice'
                                    ? 'Invoice Memo Document'
                                    : item.category === 'blueprint'
                                    ? 'CAD Technical Drawing'
                                    : 'Report Table Archive'}
                                </span>
                              </div>
                            </div>
                          )
                        ) : isPdf ? (
                          <div className="flex flex-col items-center justify-center gap-2 p-4 text-center">
                            <div className="w-14 h-14 rounded-2xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400 shadow">
                              <FileText size={28} />
                            </div>
                            <span className="font-mono text-[10px] text-rose-300 uppercase tracking-widest font-bold">
                              PDF Document Export
                            </span>
                          </div>
                        ) : (
                          <div className="flex flex-col items-center justify-center gap-2 p-4 text-center">
                            <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 shadow">
                              <FileSpreadsheet size={28} />
                            </div>
                            <span className="font-mono text-[10px] text-emerald-300 uppercase tracking-widest font-bold">
                              Spreadsheet CSV Export
                            </span>
                          </div>
                        )}

                        {/* Format Badge Overlay Top Left */}
                        <div className="absolute top-2.5 left-2.5">
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-mono font-black uppercase shadow-md ${
                              isJpg
                                ? 'bg-amber-500 text-black'
                                : isPdf
                                ? 'bg-rose-500 text-white'
                                : 'bg-emerald-500 text-white'
                            }`}
                          >
                            {item.format.toUpperCase()}
                          </span>
                        </div>

                        {/* Category Badge Top Right */}
                        <div className="absolute top-2.5 right-2.5">
                          <span className="px-2 py-0.5 rounded bg-black/70 backdrop-blur-md border border-white/10 text-[10px] font-mono uppercase text-zinc-300">
                            {item.category}
                          </span>
                        </div>

                        {/* Hover Overlay */}
                        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-2 text-white text-xs font-mono font-bold">
                          <Eye size={16} />
                          <span>Click to Inspect</span>
                        </div>
                      </div>

                      {/* Details Body */}
                      <div className="p-4 space-y-2.5">
                        <div className="flex items-start justify-between gap-2">
                          <h4 className="font-serif font-bold text-sm text-[var(--text)] line-clamp-1" title={item.title}>
                            {item.title}
                          </h4>
                        </div>

                        <p className="font-mono text-[11px] text-[var(--text-dim)] truncate" title={item.fileName}>
                          {item.fileName}
                        </p>

                        {/* Metadata Tags */}
                        <div className="flex flex-wrap items-center gap-2 text-[10px] font-mono text-[var(--text-dim)] pt-1">
                          <span className="flex items-center gap-1 bg-[var(--panel-raised)] px-2 py-0.5 rounded border border-[var(--steel-line)]">
                            <Clock size={11} />
                            <span>{new Date(item.createdAt).toLocaleDateString()} {new Date(item.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                          </span>

                          {item.fileSize && (
                            <span className="bg-[var(--panel-raised)] px-2 py-0.5 rounded border border-[var(--steel-line)] text-zinc-400">
                              {item.fileSize}
                            </span>
                          )}

                          {item.recordCount !== undefined && (
                            <span className="bg-[var(--panel-raised)] px-2 py-0.5 rounded border border-[var(--steel-line)] text-[var(--yellow)]">
                              {item.recordCount} records
                            </span>
                          )}

                          {item.totalAmount !== undefined && (
                            <span className="bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20 text-emerald-400 font-bold">
                              Rs {fmt(item.totalAmount)}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Action Buttons Footer */}
                    <div className="p-3 bg-[var(--panel-raised)] border-t border-[var(--steel-line)] flex items-center justify-between gap-2 font-mono text-xs">
                      <button
                        type="button"
                        onClick={() => setPreviewItem(item)}
                        className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-[var(--panel)] border border-[var(--steel-line)] hover:border-[var(--yellow)] text-[var(--text)] text-[11px] font-bold transition"
                      >
                        <Eye size={13} />
                        <span>Preview</span>
                      </button>

                      <div className="flex items-center gap-1.5">
                        <button
                          type="button"
                          onClick={() => handleDownloadItem(item)}
                          className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-[var(--yellow)] text-black hover:brightness-110 text-[11px] font-bold shadow transition"
                          title="Download exported file"
                        >
                          <Download size={13} />
                          <span>Download</span>
                        </button>

                        <button
                          type="button"
                          onClick={() => setItemToDelete(item)}
                          className="p-1.5 rounded-lg bg-red-500/10 border border-red-500/25 text-red-400 hover:bg-red-500/20 transition"
                          title="Delete from Gallery"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}


      {/* Blueprint Detail Inspection Modal */}
      {selectedProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-150">
          <div className="w-full max-w-2xl bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            {/* Modal Header */}
            <div className="p-4 bg-[var(--panel-raised)] border-b border-[var(--steel-line)] flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-[var(--yellow)]/15 border border-[var(--yellow)]/30 flex items-center justify-center text-[var(--yellow)]">
                  <FileText size={16} />
                </div>
                <div>
                  <h3 className="font-serif font-bold text-base text-[var(--text)]">
                    {selectedProduct.name}
                  </h3>
                  <span className="text-[10px] font-mono text-[var(--yellow)] uppercase">
                    {selectedProduct.cat} • Technical Specification Blueprint
                  </span>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setSelectedProduct(null)}
                className="w-7 h-7 rounded-lg border border-[var(--steel-line)] bg-[var(--panel)] hover:bg-red-500/10 hover:border-red-500 hover:text-red-400 text-[var(--text-dim)] flex items-center justify-center transition"
              >
                ✕
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-5 overflow-y-auto space-y-5">
              {/* Wireframe view */}
              <FanRodWireframe
                sizeInches={parseInt(selectedProduct.size || '18', 10) || 18}
                colorName={selectedProduct.color || 'Black'}
                isInspecting={true}
              />

              {/* Technical Specifications Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                <div className="bg-[var(--panel-raised)] p-2.5 rounded-lg border border-[var(--steel-line)]">
                  <span className="text-[9px] text-[var(--text-dim)] uppercase block">Length</span>
                  <span className="font-bold text-[var(--text)]">{selectedProduct.size || '18 inch'}</span>
                </div>
                <div className="bg-[var(--panel-raised)] p-2.5 rounded-lg border border-[var(--steel-line)]">
                  <span className="text-[9px] text-[var(--text-dim)] uppercase block">Weight</span>
                  <span className="font-bold text-[var(--text)]">{selectedProduct.weight || '0.75 kg'}</span>
                </div>
                <div className="bg-[var(--panel-raised)] p-2.5 rounded-lg border border-[var(--steel-line)]">
                  <span className="text-[9px] text-[var(--text-dim)] uppercase block">Color Coating</span>
                  <span className="font-bold text-[var(--text)]">{selectedProduct.color || 'Black'}</span>
                </div>
                <div className="bg-[var(--panel-raised)] p-2.5 rounded-lg border border-[var(--steel-line)]">
                  <span className="text-[9px] text-[var(--text-dim)] uppercase block">Wholesale Rate</span>
                  <span className="font-bold text-emerald-400">Rs {fmt(selectedProduct.price)}</span>
                </div>
              </div>

              {/* Bill of Materials (Recipe Breakdown) */}
              <div className="bg-[var(--panel-raised)] border border-[var(--steel-line)] rounded-xl p-4">
                <h4 className="font-serif font-bold text-xs uppercase text-[var(--yellow)] mb-3 flex items-center gap-1.5">
                  <Layers size={14} />
                  <span>Factory Bill of Materials (Recipe Items)</span>
                </h4>

                {selectedProduct.recipe && selectedProduct.recipe.length > 0 ? (
                  <div className="space-y-1.5 text-xs">
                    {selectedProduct.recipe.map((r: RecipeItem, idx: number) => (
                      <div
                        key={idx}
                        className="flex items-center justify-between py-1.5 px-2 rounded bg-black/20 border border-white/5"
                      >
                        <span className="font-mono text-[var(--text)]">{r.material}</span>
                        <span className="font-bold text-[var(--yellow)]">
                          {r.weightPerUnit ? `${r.weightPerUnit} kg` : `${r.itemsPerUnit} pcs`}
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-[var(--text-dim)] italic">
                    Standard fabrication recipe: Prime M.S. Steel Tube (16 Gauge), Pre-drilled safety cotter pin holes, hanging clamp fitting.
                  </p>
                )}
              </div>
            </div>

            {/* Modal Actions */}
            <div className="p-4 bg-[var(--panel-raised)] border-t border-[var(--steel-line)] flex items-center justify-between gap-3 flex-wrap">
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => handleExportProductBlueprintJPG(selectedProduct)}
                  disabled={isExportingBlueprint}
                  className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400 text-xs font-bold hover:bg-amber-500/20 transition disabled:opacity-50"
                >
                  {isExportingBlueprint ? (
                    <>
                      <Loader2 size={14} className="animate-spin" />
                      <span>Exporting CAD...</span>
                    </>
                  ) : blueprintSuccess ? (
                    <>
                      <CheckCircle2 size={14} className="text-emerald-400" />
                      <span className="text-emerald-400">Saved to Gallery!</span>
                    </>
                  ) : (
                    <>
                      <ImageIcon size={14} />
                      <span>Export Blueprint (JPG)</span>
                    </>
                  )}
                </button>

                <button
                  type="button"
                  onClick={handlePrintSpecSheet}
                  className="flex items-center gap-2 px-3 py-2 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] text-xs text-[var(--text-dim)] hover:text-[var(--text)] hover:border-[var(--yellow)] transition"
                >
                  <Printer size={14} />
                  <span>Print Spec Sheet</span>
                </button>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setSelectedProduct(null)}
                  className="px-3 py-2 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] text-xs text-[var(--text)] hover:bg-[var(--panel-raised)] transition"
                >
                  Close
                </button>
                <button
                  type="button"
                  onClick={() => {
                    handleAddWithFeedback(selectedProduct);
                    setSelectedProduct(null);
                  }}
                  className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[var(--yellow)] text-black font-bold text-xs hover:brightness-110 active:scale-95 shadow transition"
                >
                  <ShoppingCart size={14} />
                  <span>Add to POS Order</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Export File High-Resolution Preview Modal */}
      {previewItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4 animate-in fade-in duration-150">
          <div className="w-full max-w-4xl bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
            {/* Modal Header */}
            <div className="p-4 bg-[var(--panel-raised)] border-b border-[var(--steel-line)] flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-mono font-black uppercase ${
                    previewItem.format === 'jpg'
                      ? 'bg-amber-500 text-black'
                      : previewItem.format === 'pdf'
                      ? 'bg-rose-500 text-white'
                      : 'bg-emerald-500 text-white'
                  }`}
                >
                  {previewItem.format.toUpperCase()}
                </span>
                <div>
                  <h3 className="font-serif font-bold text-base text-[var(--text)] line-clamp-1">
                    {previewItem.title}
                  </h3>
                  <p className="font-mono text-[11px] text-[var(--text-dim)]">
                    {previewItem.fileName} • Exported on {new Date(previewItem.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setPreviewItem(null)}
                className="w-8 h-8 rounded-lg border border-[var(--steel-line)] bg-[var(--panel)] hover:bg-red-500/10 hover:border-red-500 hover:text-red-400 text-[var(--text-dim)] flex items-center justify-center transition"
              >
                ✕
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-4 overflow-y-auto max-h-[70vh] flex items-center justify-center bg-[#070b12]">
              {previewItem.format === 'jpg' ? (
                previewItem.dataUrl && !imageErrors[previewItem.id] ? (
                  <div className="flex flex-col items-center justify-center w-full">
                    <img
                      src={previewItem.dataUrl}
                      alt={previewItem.title}
                      onError={() => setImageErrors(prev => ({ ...prev, [previewItem.id]: true }))}
                      className="max-w-full max-h-[66vh] object-contain rounded-lg shadow-2xl border border-white/10"
                    />
                    <p className="mt-2 font-mono text-[11px] text-[var(--text-dim)]">
                      Full High-Resolution Graphic Archive • Click download to save raw image
                    </p>
                  </div>
                ) : (
                  <div className="p-8 text-center space-y-4 max-w-md">
                    <div className="w-20 h-20 rounded-3xl mx-auto flex items-center justify-center bg-amber-500/15 border border-amber-500/35 text-amber-400">
                      <ImageIcon size={40} />
                    </div>
                    <div>
                      <h4 className="font-serif font-bold text-lg text-[var(--text)]">{previewItem.title}</h4>
                      <p className="text-xs text-[var(--text-dim)] font-mono mt-1">
                        {previewItem.description || 'Graphical JPG export archived'}
                      </p>
                    </div>
                    <div className="bg-[var(--panel)] p-4 rounded-xl border border-[var(--steel-line)] text-xs font-mono space-y-1.5 text-left">
                      <div className="flex justify-between">
                        <span className="text-[var(--text-dim)]">Format:</span>
                        <span className="font-bold uppercase text-amber-400">{previewItem.format}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-[var(--text-dim)]">File Name:</span>
                        <span className="font-bold text-[var(--text)] truncate max-w-[200px]">{previewItem.fileName}</span>
                      </div>
                      {previewItem.fileSize && (
                        <div className="flex justify-between">
                          <span className="text-[var(--text-dim)]">Estimated Size:</span>
                          <span className="text-[var(--text)]">{previewItem.fileSize}</span>
                        </div>
                      )}
                      {previewItem.recordCount !== undefined && (
                        <div className="flex justify-between">
                          <span className="text-[var(--text-dim)]">Rows / Records:</span>
                          <span className="text-[var(--text)]">{previewItem.recordCount}</span>
                        </div>
                      )}
                      {previewItem.totalAmount !== undefined && (
                        <div className="flex justify-between">
                          <span className="text-[var(--text-dim)]">Total Amount:</span>
                          <span className="font-bold text-emerald-400">Rs {fmt(previewItem.totalAmount)}</span>
                        </div>
                      )}
                    </div>
                  </div>
                )
              ) : (
                <div className="p-8 text-center space-y-4 max-w-md">
                  <div className="w-20 h-20 rounded-3xl mx-auto flex items-center justify-center bg-[var(--panel-raised)] border border-[var(--steel-line)]">
                    {previewItem.format === 'pdf' ? (
                      <FileText size={40} className="text-rose-400" />
                    ) : (
                      <FileSpreadsheet size={40} className="text-emerald-400" />
                    )}
                  </div>
                  <div>
                    <h4 className="font-serif font-bold text-lg text-[var(--text)]">{previewItem.title}</h4>
                    <p className="text-xs text-[var(--text-dim)] font-mono mt-1">{previewItem.description || 'Export document archived'}</p>
                  </div>
                  <div className="bg-[var(--panel)] p-4 rounded-xl border border-[var(--steel-line)] text-xs font-mono space-y-1.5 text-left">
                    <div className="flex justify-between">
                      <span className="text-[var(--text-dim)]">Format:</span>
                      <span className="font-bold uppercase text-[var(--yellow)]">{previewItem.format}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-[var(--text-dim)]">File Name:</span>
                      <span className="font-bold text-[var(--text)] truncate max-w-[200px]">{previewItem.fileName}</span>
                    </div>
                    {previewItem.fileSize && (
                      <div className="flex justify-between">
                        <span className="text-[var(--text-dim)]">Estimated Size:</span>
                        <span className="text-[var(--text)]">{previewItem.fileSize}</span>
                      </div>
                    )}
                    {previewItem.recordCount !== undefined && (
                      <div className="flex justify-between">
                        <span className="text-[var(--text-dim)]">Rows / Records:</span>
                        <span className="text-[var(--text)]">{previewItem.recordCount}</span>
                      </div>
                    )}
                    {previewItem.totalAmount !== undefined && (
                      <div className="flex justify-between">
                        <span className="text-[var(--text-dim)]">Total Amount:</span>
                        <span className="font-bold text-emerald-400">Rs {fmt(previewItem.totalAmount)}</span>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Modal Actions */}
            <div className="p-4 bg-[var(--panel-raised)] border-t border-[var(--steel-line)] flex items-center justify-between gap-3 font-mono">
              <button
                type="button"
                onClick={() => {
                  setItemToDelete(previewItem);
                }}
                className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 hover:bg-red-500/20 text-xs font-bold transition"
              >
                <Trash2 size={14} />
                <span>Delete from Archive</span>
              </button>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setPreviewItem(null)}
                  className="px-4 py-2 rounded-xl bg-[var(--panel)] border border-[var(--steel-line)] text-xs text-[var(--text)] hover:bg-[var(--panel-raised)] transition"
                >
                  Close
                </button>
                <button
                  type="button"
                  onClick={() => handleDownloadItem(previewItem)}
                  className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[var(--yellow)] text-black font-bold text-xs hover:brightness-110 shadow transition"
                >
                  <Download size={14} />
                  <span>Download {previewItem.format.toUpperCase()}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {itemToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-150 font-sans">
          <div className="w-full max-w-md bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl p-5 shadow-2xl space-y-4">
            <div className="flex items-center gap-3 text-red-400">
              <div className="w-10 h-10 rounded-xl bg-red-500/10 border border-red-500/30 flex items-center justify-center">
                <AlertTriangle size={20} />
              </div>
              <div>
                <h3 className="font-serif font-bold text-base text-[var(--text)]">Delete Exported File?</h3>
                <p className="text-xs text-[var(--text-dim)] font-mono">This will remove the file from your gallery archive.</p>
              </div>
            </div>

            <div className="p-3 bg-[var(--panel-raised)] rounded-xl border border-[var(--steel-line)] text-xs font-mono space-y-1">
              <p className="font-bold text-[var(--text)] line-clamp-1">{itemToDelete.title}</p>
              <p className="text-[var(--text-dim)] text-[11px] truncate">{itemToDelete.fileName}</p>
            </div>

            <div className="flex items-center justify-end gap-2 pt-1 font-mono text-xs">
              <button
                type="button"
                onClick={() => setItemToDelete(null)}
                className="px-4 py-2 rounded-xl bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[var(--text)] hover:bg-[var(--panel)] transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleDeleteConfirmed}
                className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-red-600 hover:bg-red-500 text-white font-bold transition shadow"
              >
                <Trash2 size={13} />
                <span>Confirm Delete</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Clear All Confirmation Modal */}
      {showClearAllConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-150 font-sans">
          <div className="w-full max-w-md bg-[var(--panel)] border border-[var(--steel-line)] rounded-2xl p-5 shadow-2xl space-y-4">
            <div className="flex items-center gap-3 text-red-400">
              <div className="w-10 h-10 rounded-xl bg-red-500/10 border border-red-500/30 flex items-center justify-center">
                <Trash2 size={20} />
              </div>
              <div>
                <h3 className="font-serif font-bold text-base text-[var(--text)]">Clear All Gallery Exports?</h3>
                <p className="text-xs text-[var(--text-dim)] font-mono">This will permanently remove all {exportedItems.length} exported files from storage.</p>
              </div>
            </div>

            <p className="text-xs text-[var(--text-dim)]">
              You can always re-export invoices, reports, or blueprints anytime from the Point of Sale, Ledgers, or Blueprint Designer.
            </p>

            <div className="flex items-center justify-end gap-2 pt-2 font-mono text-xs">
              <button
                type="button"
                onClick={() => setShowClearAllConfirm(false)}
                className="px-4 py-2 rounded-xl bg-[var(--panel-raised)] border border-[var(--steel-line)] text-[var(--text)] hover:bg-[var(--panel)] transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleClearAllConfirmed}
                className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-red-600 hover:bg-red-500 text-white font-bold transition shadow"
              >
                <Trash2 size={13} />
                <span>Clear All Files</span>
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default GalleryView;

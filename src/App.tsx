import { useMemo, useState } from 'react'
import {
  ArrowRight,
  Bell,
  Check,
  ChevronLeft,
  CircleUserRound,
  Clock3,
  Download,
  Gem,
  Heart,
  Home,
  MessageCircle,
  Plus,
  ReceiptText,
  Search,
  Share2,
  ShieldCheck,
  ShoppingBag,
  Sparkles,
  Store,
  Trash2,
  UserRound,
  X,
} from 'lucide-react'
import {
  calculateInvoice,
  formatToman,
  toPersianNumber,
  type InvoiceItem,
} from './invoice'

type Category = 'همه' | 'انگشتر' | 'گردنبند' | 'دستبند' | 'گوشواره'
type Tab = 'home' | 'shop' | 'invoices' | 'profile'

type Product = Omit<InvoiceItem, 'quantity'> & {
  category: Exclude<Category, 'همه'>
  image: string
  subtitle: string
  badge?: string
}

type IssuedInvoice = {
  number: string
  customer: string
  phone: string
  createdAt: string
  total: number
  itemCount: number
}

const GOLD_PRICE = 5_842_000

const products: Product[] = [
  {
    id: 'ring-aura',
    name: 'انگشتر اورا',
    subtitle: 'طلای زرد ۱۸ عیار',
    category: 'انگشتر',
    weight: 3.18,
    wagePercent: 14,
    badge: 'پرفروش',
    image:
      'https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=720&q=88',
  },
  {
    id: 'necklace-luna',
    name: 'گردنبند لونا',
    subtitle: 'طلای زرد ۱۸ عیار',
    category: 'گردنبند',
    weight: 5.42,
    wagePercent: 16,
    badge: 'جدید',
    image:
      'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=720&q=88',
  },
  {
    id: 'bracelet-roya',
    name: 'دستبند رویا',
    subtitle: 'طلای زرد ۱۸ عیار',
    category: 'دستبند',
    weight: 4.76,
    wagePercent: 13,
    image:
      'https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=720&q=88',
  },
  {
    id: 'earring-venus',
    name: 'گوشواره ونوس',
    subtitle: 'طلای زرد ۱۸ عیار',
    category: 'گوشواره',
    weight: 2.64,
    wagePercent: 17,
    image:
      'https://images.unsplash.com/photo-1617038220319-276d3cfab638?auto=format&fit=crop&w=720&q=88',
  },
  {
    id: 'ring-sol',
    name: 'انگشتر سُل',
    subtitle: 'طلای سفید ۱۸ عیار',
    category: 'انگشتر',
    weight: 3.85,
    wagePercent: 15,
    image:
      'https://images.unsplash.com/photo-1603561596112-db1d6d140b8c?auto=format&fit=crop&w=720&q=88',
  },
  {
    id: 'necklace-mira',
    name: 'گردنبند میرا',
    subtitle: 'طلای زرد ۱۸ عیار',
    category: 'گردنبند',
    weight: 6.11,
    wagePercent: 12,
    image:
      'https://images.unsplash.com/photo-1599459183200-59c7687a0275?auto=format&fit=crop&w=720&q=88',
  },
]

const categories: Category[] = ['همه', 'انگشتر', 'گردنبند', 'دستبند', 'گوشواره']

const productTotal = (product: Product) =>
  calculateInvoice([{ ...product, quantity: 1 }], GOLD_PRICE).total

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('home')
  const [category, setCategory] = useState<Category>('همه')
  const [search, setSearch] = useState('')
  const [cart, setCart] = useState<string[]>(['ring-aura'])
  const [favorites, setFavorites] = useState<string[]>(['necklace-luna'])
  const [invoiceOpen, setInvoiceOpen] = useState(false)
  const [issuedInvoices, setIssuedInvoices] = useState<IssuedInvoice[]>([])
  const [toast, setToast] = useState('')

  const cartProducts = products.filter((product) => cart.includes(product.id))

  const visibleProducts = useMemo(() => {
    const normalizedSearch = search.trim()
    return products.filter(
      (product) =>
        (category === 'همه' || product.category === category) &&
        (!normalizedSearch ||
          product.name.includes(normalizedSearch) ||
          product.category.includes(normalizedSearch)),
    )
  }, [category, search])

  const showToast = (message: string) => {
    setToast(message)
    window.setTimeout(() => setToast(''), 2400)
  }

  const addToCart = (id: string) => {
    if (cart.includes(id)) {
      showToast('این محصول در فاکتور شماست')
      return
    }
    setCart((current) => [...current, id])
    showToast('به فاکتور اضافه شد')
  }

  const toggleFavorite = (id: string) => {
    setFavorites((current) =>
      current.includes(id)
        ? current.filter((favoriteId) => favoriteId !== id)
        : [...current, id],
    )
  }

  const changeTab = (tab: Tab) => {
    setActiveTab(tab)
    if (tab !== 'shop') {
      setSearch('')
      setCategory('همه')
    }
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleInvoiceIssued = (invoice: IssuedInvoice) => {
    setIssuedInvoices((current) => [invoice, ...current])
  }

  return (
    <div className="app-shell">
      <div className="app">
        {activeTab === 'home' && (
          <HomeView
            cartCount={cart.length}
            favorites={favorites}
            onFavorite={toggleFavorite}
            onAdd={addToCart}
            onShop={() => changeTab('shop')}
            onInvoice={() => setInvoiceOpen(true)}
          />
        )}

        {activeTab === 'shop' && (
          <ShopView
            category={category}
            search={search}
            products={visibleProducts}
            favorites={favorites}
            cartCount={cart.length}
            onSearch={setSearch}
            onCategory={setCategory}
            onFavorite={toggleFavorite}
            onAdd={addToCart}
            onCart={() => setInvoiceOpen(true)}
          />
        )}

        {activeTab === 'invoices' && (
          <InvoicesView
            invoices={issuedInvoices}
            onCreate={() => setInvoiceOpen(true)}
          />
        )}

        {activeTab === 'profile' && <ProfileView />}

        <BottomNavigation
          activeTab={activeTab}
          onChange={changeTab}
          onInvoice={() => setInvoiceOpen(true)}
        />

        {invoiceOpen && (
          <InvoiceBuilder
            products={cartProducts}
            onRemove={(id) =>
              setCart((current) => current.filter((itemId) => itemId !== id))
            }
            onAdd={(id) => addToCart(id)}
            onClose={() => setInvoiceOpen(false)}
            onIssued={handleInvoiceIssued}
            showToast={showToast}
          />
        )}

        {toast && (
          <div className="toast" role="status">
            <span className="toast-check">
              <Check size={14} strokeWidth={3} />
            </span>
            {toast}
          </div>
        )}
      </div>
    </div>
  )
}

type HomeViewProps = {
  cartCount: number
  favorites: string[]
  onFavorite: (id: string) => void
  onAdd: (id: string) => void
  onShop: () => void
  onInvoice: () => void
}

function HomeView({
  cartCount,
  favorites,
  onFavorite,
  onAdd,
  onShop,
  onInvoice,
}: HomeViewProps) {
  return (
    <main className="screen home-screen">
      <header className="home-header">
        <div className="brand">
          <span className="brand-mark">
            <Gem size={21} />
          </span>
          <div>
            <strong>زرین</strong>
            <small>طلا برای لحظه‌های ماندگار</small>
          </div>
        </div>
        <button className="icon-button notification-button" aria-label="اعلان‌ها">
          <Bell size={21} />
          <span className="notification-dot" />
        </button>
      </header>

      <section className="gold-rate-card">
        <div className="rate-heading">
          <div className="live-label">
            <span />
            قیمت لحظه‌ای
          </div>
          <span className="rate-change">٪ ۰٫۸ +</span>
        </div>
        <div className="rate-content">
          <div>
            <small>هر گرم طلای ۱۸ عیار</small>
            <strong>{formatToman(GOLD_PRICE)}</strong>
          </div>
          <div className="sparkline" aria-hidden="true">
            <svg viewBox="0 0 94 42">
              <path d="M2 35 C13 28, 17 34, 28 24 S42 31, 50 18 S67 23, 76 11 S87 13, 92 4" />
              <path
                className="sparkline-fill"
                d="M2 35 C13 28, 17 34, 28 24 S42 31, 50 18 S67 23, 76 11 S87 13, 92 4 L92 42 L2 42 Z"
              />
            </svg>
          </div>
        </div>
        <div className="rate-footer">
          <span>
            <Clock3 size={13} />
            بروزرسانی: همین حالا
          </span>
          <span>بازار باز است</span>
        </div>
      </section>

      <section className="hero-card">
        <div className="hero-content">
          <span className="eyebrow">کالکشن تابستان ۱۴۰۵</span>
          <h1>
            درخشش تو،
            <br />
            امضای توست.
          </h1>
          <p>طراحی‌های ظریف برای زیبایی هر روز</p>
          <button onClick={onShop}>
            مشاهده کالکشن
            <ChevronLeft size={17} />
          </button>
        </div>
        <div className="hero-glow" />
      </section>

      <section className="quick-actions">
        <button onClick={onShop}>
          <span className="quick-action-icon">
            <Store size={22} />
          </span>
          <span>
            <strong>ویترین طلا</strong>
            <small>خرید از کالکشن‌ها</small>
          </span>
          <ChevronLeft size={18} />
        </button>
        <button onClick={onInvoice}>
          <span className="quick-action-icon invoice-action-icon">
            <ReceiptText size={22} />
            {cartCount > 0 && <i>{toPersianNumber(cartCount)}</i>}
          </span>
          <span>
            <strong>صدور فاکتور</strong>
            <small>سریع و معتبر</small>
          </span>
          <ChevronLeft size={18} />
        </button>
      </section>

      <section className="section-block">
        <div className="section-title">
          <div>
            <span className="title-accent" />
            <h2>منتخب امروز</h2>
          </div>
          <button onClick={onShop}>
            مشاهده همه
            <ChevronLeft size={15} />
          </button>
        </div>
        <div className="product-grid">
          {products.slice(0, 4).map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              isFavorite={favorites.includes(product.id)}
              onFavorite={onFavorite}
              onAdd={onAdd}
            />
          ))}
        </div>
      </section>

      <section className="trust-banner">
        <span>
          <ShieldCheck size={23} />
        </span>
        <div>
          <strong>خریدی امن و شفاف</strong>
          <p>ضمانت اصالت، فاکتور معتبر و ارسال بیمه‌شده</p>
        </div>
      </section>
    </main>
  )
}

type ShopViewProps = {
  category: Category
  search: string
  products: Product[]
  favorites: string[]
  cartCount: number
  onSearch: (value: string) => void
  onCategory: (category: Category) => void
  onFavorite: (id: string) => void
  onAdd: (id: string) => void
  onCart: () => void
}

function ShopView({
  category,
  search,
  products: visibleProducts,
  favorites,
  cartCount,
  onSearch,
  onCategory,
  onFavorite,
  onAdd,
  onCart,
}: ShopViewProps) {
  return (
    <main className="screen shop-screen">
      <header className="page-header">
        <div>
          <span>کالکشن زرین</span>
          <h1>ویترین طلا</h1>
        </div>
        <button className="icon-button cart-button" onClick={onCart} aria-label="فاکتور">
          <ShoppingBag size={21} />
          {cartCount > 0 && <i>{toPersianNumber(cartCount)}</i>}
        </button>
      </header>

      <label className="search-box">
        <Search size={20} />
        <input
          value={search}
          onChange={(event) => onSearch(event.target.value)}
          placeholder="جستجو میان زیورآلات..."
        />
      </label>

      <div className="category-scroll">
        {categories.map((item) => (
          <button
            className={category === item ? 'active' : ''}
            key={item}
            onClick={() => onCategory(item)}
          >
            {item}
          </button>
        ))}
      </div>

      <div className="results-heading">
        <strong>{toPersianNumber(visibleProducts.length)} محصول</strong>
        <span>مرتب‌سازی: جدیدترین</span>
      </div>

      {visibleProducts.length > 0 ? (
        <div className="product-grid shop-product-grid">
          {visibleProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              isFavorite={favorites.includes(product.id)}
              onFavorite={onFavorite}
              onAdd={onAdd}
            />
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <Search size={28} />
          <strong>محصولی پیدا نشد</strong>
          <p>عبارت دیگری را امتحان کنید.</p>
        </div>
      )}
    </main>
  )
}

type ProductCardProps = {
  product: Product
  isFavorite: boolean
  onFavorite: (id: string) => void
  onAdd: (id: string) => void
}

function ProductCard({
  product,
  isFavorite,
  onFavorite,
  onAdd,
}: ProductCardProps) {
  return (
    <article className="product-card">
      <div className="product-image">
        <img src={product.image} alt={product.name} />
        {product.badge && <span className="product-badge">{product.badge}</span>}
        <button
          className={isFavorite ? 'favorite active' : 'favorite'}
          onClick={() => onFavorite(product.id)}
          aria-label={isFavorite ? 'حذف از علاقه‌مندی' : 'افزودن به علاقه‌مندی'}
        >
          <Heart size={17} fill={isFavorite ? 'currentColor' : 'none'} />
        </button>
      </div>
      <div className="product-details">
        <h3>{product.name}</h3>
        <p>{product.subtitle}</p>
        <span className="product-weight">
          {toPersianNumber(product.weight)} گرم
        </span>
        <div className="product-price">
          <strong>{formatToman(productTotal(product))}</strong>
          <button
            onClick={() => onAdd(product.id)}
            aria-label={`افزودن ${product.name} به فاکتور`}
          >
            <Plus size={18} />
          </button>
        </div>
      </div>
    </article>
  )
}

function InvoicesView({
  invoices,
  onCreate,
}: {
  invoices: IssuedInvoice[]
  onCreate: () => void
}) {
  return (
    <main className="screen invoices-screen">
      <header className="page-header">
        <div>
          <span>مدیریت خریدها</span>
          <h1>فاکتورهای من</h1>
        </div>
        <button className="icon-button">
          <ReceiptText size={21} />
        </button>
      </header>

      <section className="invoice-summary-card">
        <div>
          <span>مجموع خرید شما</span>
          <strong>
            {formatToman(
              invoices.reduce((sum, invoice) => sum + invoice.total, 0),
            )}
          </strong>
        </div>
        <span className="summary-gem">
          <Sparkles size={27} />
        </span>
      </section>

      <div className="invoice-list-heading">
        <h2>فاکتورهای اخیر</h2>
        <span>{toPersianNumber(invoices.length)} فاکتور</span>
      </div>

      {invoices.length === 0 ? (
        <div className="empty-invoices">
          <span>
            <ReceiptText size={31} />
          </span>
          <h3>هنوز فاکتوری ندارید</h3>
          <p>محصول مورد علاقه‌تان را انتخاب کنید و یک فاکتور رسمی بسازید.</p>
          <button onClick={onCreate}>
            <Plus size={18} />
            صدور اولین فاکتور
          </button>
        </div>
      ) : (
        <div className="invoice-list">
          {invoices.map((invoice) => (
            <article key={invoice.number} className="invoice-row">
              <span className="invoice-row-icon">
                <ReceiptText size={21} />
              </span>
              <div>
                <strong>فاکتور {invoice.number}</strong>
                <small>
                  {invoice.createdAt} · {toPersianNumber(invoice.itemCount)} کالا
                </small>
              </div>
              <div className="invoice-row-total">
                <strong>{formatToman(invoice.total)}</strong>
                <span>تکمیل شده</span>
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  )
}

function ProfileView() {
  return (
    <main className="screen profile-screen">
      <header className="page-header">
        <div>
          <span>حساب کاربری</span>
          <h1>پروفایل من</h1>
        </div>
        <button className="icon-button">
          <UserRound size={21} />
        </button>
      </header>

      <section className="profile-card">
        <div className="profile-avatar">
          <CircleUserRound size={40} />
        </div>
        <div>
          <h2>نگار احمدی</h2>
          <p>۰۹۱۲ ۴۵۶ ۷۸۹۰</p>
          <span>
            <ShieldCheck size={14} />
            حساب تأیید شده
          </span>
        </div>
        <ChevronLeft size={20} />
      </section>

      <section className="membership-card">
        <div>
          <Sparkles size={19} />
          <span>باشگاه مشتریان زرین</span>
        </div>
        <strong>۲٬۸۴۰ امتیاز</strong>
        <p>فقط ۱۶۰ امتیاز تا هدیه بعدی</p>
        <div className="membership-progress">
          <span />
        </div>
      </section>

      <section className="profile-menu">
        <button>
          <span>
            <ShoppingBag size={20} />
          </span>
          <div>
            <strong>سفارش‌های من</strong>
            <small>پیگیری و مشاهده جزئیات</small>
          </div>
          <ChevronLeft size={19} />
        </button>
        <button>
          <span>
            <Heart size={20} />
          </span>
          <div>
            <strong>علاقه‌مندی‌ها</strong>
            <small>زیورآلاتی که دوست داشتید</small>
          </div>
          <ChevronLeft size={19} />
        </button>
        <button>
          <span>
            <MessageCircle size={20} />
          </span>
          <div>
            <strong>پشتیبانی زرین</strong>
            <small>همیشه پاسخگوی شما هستیم</small>
          </div>
          <ChevronLeft size={19} />
        </button>
      </section>
    </main>
  )
}

function BottomNavigation({
  activeTab,
  onChange,
  onInvoice,
}: {
  activeTab: Tab
  onChange: (tab: Tab) => void
  onInvoice: () => void
}) {
  return (
    <nav className="bottom-nav">
      <button
        className={activeTab === 'home' ? 'active' : ''}
        onClick={() => onChange('home')}
      >
        <Home size={21} />
        <span>خانه</span>
      </button>
      <button
        className={activeTab === 'shop' ? 'active' : ''}
        onClick={() => onChange('shop')}
      >
        <Store size={21} />
        <span>ویترین</span>
      </button>
      <button className="nav-invoice" onClick={onInvoice}>
        <span>
          <ReceiptText size={23} />
        </span>
        <em>فاکتور</em>
      </button>
      <button
        className={activeTab === 'invoices' ? 'active' : ''}
        onClick={() => onChange('invoices')}
      >
        <ReceiptText size={21} />
        <span>خریدها</span>
      </button>
      <button
        className={activeTab === 'profile' ? 'active' : ''}
        onClick={() => onChange('profile')}
      >
        <UserRound size={21} />
        <span>پروفایل</span>
      </button>
    </nav>
  )
}

type InvoiceBuilderProps = {
  products: Product[]
  onRemove: (id: string) => void
  onAdd: (id: string) => void
  onClose: () => void
  onIssued: (invoice: IssuedInvoice) => void
  showToast: (message: string) => void
}

function InvoiceBuilder({
  products: invoiceProducts,
  onRemove,
  onAdd,
  onClose,
  onIssued,
  showToast,
}: InvoiceBuilderProps) {
  const [customer, setCustomer] = useState('')
  const [phone, setPhone] = useState('')
  const [confirmed, setConfirmed] = useState(true)
  const [success, setSuccess] = useState<IssuedInvoice | null>(null)

  const invoiceItems = invoiceProducts.map((product) => ({
    ...product,
    quantity: 1,
  }))
  const totals = calculateInvoice(invoiceItems, GOLD_PRICE)

  const issueInvoice = () => {
    if (!customer.trim()) {
      showToast('نام خریدار را وارد کنید')
      return
    }
    if (invoiceItems.length === 0) {
      showToast('حداقل یک محصول اضافه کنید')
      return
    }

    const invoice: IssuedInvoice = {
      number: `۱۴۰۵-${toPersianNumber(String(Date.now()).slice(-4))}`,
      customer: customer.trim(),
      phone: phone.trim(),
      createdAt: 'امروز، ۲۰:۴۲',
      total: totals.total,
      itemCount: invoiceItems.length,
    }
    onIssued(invoice)
    setSuccess(invoice)
  }

  const shareInvoice = async () => {
    if (!success) return
    const shareData = {
      title: `فاکتور زرین ${success.number}`,
      text: `فاکتور خرید ${success.customer} به مبلغ ${formatToman(success.total)}`,
    }
    try {
      if (navigator.share) {
        await navigator.share(shareData)
      } else {
        await navigator.clipboard.writeText(shareData.text)
        showToast('خلاصه فاکتور کپی شد')
      }
    } catch {
      // Closing the native share sheet should not surface an error.
    }
  }

  if (success) {
    return (
      <div className="invoice-overlay">
        <div className="success-view">
          <button className="success-close" onClick={onClose} aria-label="بستن">
            <X size={22} />
          </button>
          <div className="success-mark">
            <Check size={38} strokeWidth={2.5} />
          </div>
          <span className="success-kicker">عملیات موفق</span>
          <h2>فاکتور شما صادر شد</h2>
          <p>نسخه دیجیتال فاکتور در بخش خریدهای شما ذخیره شد.</p>

          <article className="success-receipt">
            <div className="receipt-brand">
              <span>
                <Gem size={19} />
              </span>
              <strong>زرین</strong>
              <small>فاکتور رسمی فروش</small>
            </div>
            <div className="receipt-dashes" />
            <div className="receipt-info">
              <span>
                <small>شماره فاکتور</small>
                <strong>{success.number}</strong>
              </span>
              <span>
                <small>خریدار</small>
                <strong>{success.customer}</strong>
              </span>
            </div>
            <div className="receipt-total">
              <span>مبلغ نهایی</span>
              <strong>{formatToman(success.total)}</strong>
            </div>
            <div className="receipt-verification">
              <ShieldCheck size={16} />
              اصالت این فاکتور توسط زرین تضمین شده است
            </div>
          </article>

          <button
            className="primary-button"
            onClick={() => {
              onClose()
              showToast('فاکتور در خریدهای شما ذخیره شد')
            }}
          >
            <Download size={19} />
            دریافت فاکتور
          </button>
          <button className="secondary-button" onClick={shareInvoice}>
            <Share2 size={18} />
            اشتراک‌گذاری
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="invoice-overlay">
      <div className="invoice-builder">
        <header className="invoice-header">
          <button onClick={onClose} aria-label="بازگشت">
            <ArrowRight size={22} />
          </button>
          <div>
            <h2>صدور فاکتور</h2>
            <span>فاکتور فروش رسمی زرین</span>
          </div>
          <span className="secure-chip">
            <ShieldCheck size={14} />
            امن
          </span>
        </header>

        <div className="invoice-progress">
          <span className="active" />
          <span className="active" />
          <span />
        </div>

        <div className="invoice-content">
          <section className="form-section">
            <div className="form-section-title">
              <span>۱</span>
              <div>
                <h3>مشخصات خریدار</h3>
                <p>اطلاعات روی فاکتور ثبت می‌شود</p>
              </div>
            </div>
            <div className="customer-form">
              <label>
                نام و نام خانوادگی
                <input
                  value={customer}
                  onChange={(event) => setCustomer(event.target.value)}
                  placeholder="مثلاً نگار احمدی"
                  autoFocus
                />
              </label>
              <label>
                شماره موبایل
                <input
                  value={phone}
                  onChange={(event) => setPhone(event.target.value)}
                  placeholder="۰۹۱۲ ۱۲۳ ۴۵۶۷"
                  inputMode="tel"
                />
              </label>
            </div>
          </section>

          <section className="form-section">
            <div className="form-section-title">
              <span>۲</span>
              <div>
                <h3>اقلام فاکتور</h3>
                <p>{toPersianNumber(invoiceProducts.length)} محصول انتخاب شده</p>
              </div>
            </div>

            {invoiceProducts.length > 0 ? (
              <div className="invoice-products">
                {invoiceProducts.map((product) => (
                  <article key={product.id}>
                    <img src={product.image} alt="" />
                    <div>
                      <strong>{product.name}</strong>
                      <small>
                        {toPersianNumber(product.weight)} گرم · اجرت ٪
                        {toPersianNumber(product.wagePercent)}
                      </small>
                      <span>{formatToman(productTotal(product))}</span>
                    </div>
                    <button
                      onClick={() => onRemove(product.id)}
                      aria-label={`حذف ${product.name}`}
                    >
                      <Trash2 size={17} />
                    </button>
                  </article>
                ))}
              </div>
            ) : (
              <div className="empty-cart">
                <ShoppingBag size={25} />
                <span>هنوز محصولی انتخاب نکرده‌اید</span>
              </div>
            )}

            <div className="add-product-row">
              <span>افزودن سریع:</span>
              {products
                .filter(
                  (product) =>
                    !invoiceProducts.some((item) => item.id === product.id),
                )
                .slice(0, 2)
                .map((product) => (
                  <button key={product.id} onClick={() => onAdd(product.id)}>
                    <Plus size={14} />
                    {product.name}
                  </button>
                ))}
            </div>
          </section>

          <section className="form-section">
            <div className="form-section-title">
              <span>۳</span>
              <div>
                <h3>محاسبه مبلغ</h3>
                <p>شفاف و بر اساس نرخ لحظه‌ای</p>
              </div>
            </div>

            <div className="gold-price-row">
              <div>
                <span className="live-dot" />
                <label>نرخ هر گرم طلای ۱۸ عیار</label>
              </div>
              <strong>{formatToman(GOLD_PRICE)}</strong>
            </div>

            <div className="calculation-card">
              <div>
                <span>وزن کل</span>
                <strong>{toPersianNumber(totals.weight)} گرم</strong>
              </div>
              <div>
                <span>ارزش طلا</span>
                <strong>{formatToman(totals.goldValue)}</strong>
              </div>
              <div>
                <span>اجرت ساخت</span>
                <strong>{formatToman(totals.wage)}</strong>
              </div>
              <div>
                <span>سود فروشنده (٪۷)</span>
                <strong>{formatToman(totals.profit)}</strong>
              </div>
              <div>
                <span>مالیات ارزش افزوده (٪۱۰)</span>
                <strong>{formatToman(totals.tax)}</strong>
              </div>
              <div className="calculation-total">
                <span>مبلغ قابل پرداخت</span>
                <strong>{formatToman(totals.total)}</strong>
              </div>
            </div>
          </section>

          <label className="confirmation-row">
            <button
              type="button"
              className={confirmed ? 'checkbox checked' : 'checkbox'}
              onClick={() => setConfirmed((value) => !value)}
              aria-label="تأیید اطلاعات"
            >
              {confirmed && <Check size={14} />}
            </button>
            <span>
              صحت اطلاعات فاکتور و قوانین خرید زرین را می‌پذیرم.
            </span>
          </label>
        </div>

        <footer className="invoice-footer">
          <div>
            <span>مبلغ نهایی</span>
            <strong>{formatToman(totals.total)}</strong>
          </div>
          <button
            className="issue-button"
            disabled={!confirmed || invoiceProducts.length === 0}
            onClick={issueInvoice}
          >
            صدور فاکتور
            <ChevronLeft size={19} />
          </button>
        </footer>
      </div>
    </div>
  )
}

export default App

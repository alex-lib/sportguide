import {
  Home,
  Calendar,
  MapPin,
  Users,
  GraduationCap,
  ClipboardList,
  Ellipsis,
  Search,
  ArrowUpDown,
  ChevronDown,
  ChevronRight,
  ChevronUp,
  Clock,
  Star,
  Plus,
  Pencil,
  Trash2,
  Award,
  User,
  Settings,
  Map,
  Building,
  Building2,
  TriangleAlert,
  Footprints,
  Goal,
  Dumbbell,
  Waves,
  PersonStanding,
  Swords,
  CircleDot,
  Target,
  LandPlot,
  X,
  SlidersHorizontal,
  Filter,
  Phone,
  MessageCircle,
  Globe,
  ArrowRight,
  Check,
} from 'lucide-react';

/*
 * The exact Lucide semantic icon set used across the app (see design/).
 * Icons are outline, 2px stroke, currentColor, sized to the surrounding text.
 * Add a new icon here (and only from Lucide) rather than importing ad-hoc.
 */
const ICONS = {
  home: Home,
  calendar: Calendar,
  'map-pin': MapPin,
  users: Users,
  'graduation-cap': GraduationCap,
  'clipboard-list': ClipboardList,
  ellipsis: Ellipsis,
  search: Search,
  'arrow-up-down': ArrowUpDown,
  'chevron-down': ChevronDown,
  'chevron-right': ChevronRight,
  'chevron-up': ChevronUp,
  clock: Clock,
  star: Star,
  plus: Plus,
  pencil: Pencil,
  'trash-2': Trash2,
  award: Award,
  user: User,
  settings: Settings,
  map: Map,
  building: Building,
  'building-2': Building2,
  'triangle-alert': TriangleAlert,
  footprints: Footprints,
  goal: Goal,
  dumbbell: Dumbbell,
  waves: Waves,
  'person-standing': PersonStanding,
  swords: Swords,
  'circle-dot': CircleDot,
  target: Target,
  'land-plot': LandPlot,
  x: X,
  'sliders-horizontal': SlidersHorizontal,
  filter: Filter,
  phone: Phone,
  'message-circle': MessageCircle,
  globe: Globe,
  'arrow-right': ArrowRight,
  check: Check,
};

/**
 * Icon - render a Lucide glyph by its semantic name.
 * `size` defaults to 1em so the icon scales with surrounding text.
 */
const Icon = ({ name, size = '1em', strokeWidth = 2, className = '', ...rest }) => {
  const Glyph = ICONS[name];
  if (!Glyph) {
    if (import.meta.env?.DEV) console.warn(`Icon: unknown name "${name}"`);
    return null;
  }
  return <Glyph size={size} strokeWidth={strokeWidth} className={`ui-icon ${className}`.trim()} {...rest} />;
};

/** Map a Russian sport-type label to its Lucide icon name. */
export const sportIconName = (sport = '') => {
  const s = sport.toLowerCase();
  if (s.includes('бег') || s.includes('легкая атлетика') || s.includes('воркаут')) return 'footprints';
  if (s.includes('футбол')) return 'goal';
  if (s.includes('баскетбол')) return 'circle-dot';
  if (s.includes('теннис') || s.includes('падел') || s.includes('пинг')) return 'target';
  if (s.includes('бокс') || s.includes('мма') || s.includes('борьба') || s.includes('джиу') || s.includes('единобор'))
    return 'swords';
  if (s.includes('плав') || s.includes('бассейн')) return 'waves';
  if (s.includes('йога') || s.includes('растяж') || s.includes('гимнаст') || s.includes('фигурн'))
    return 'person-standing';
  if (s.includes('фитнес') || s.includes('зал') || s.includes('сил')) return 'dumbbell';
  return 'goal';
};

export default Icon;

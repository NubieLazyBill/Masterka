package com.example.masterka.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object IconRegistry {

    /**
     * Словарь: имя иконки → ImageVector.
     * Имена — точно как в Material Icons.
     */
    val icons: Map<String, ImageVector> = mapOf(
        // ==================== ИНСТРУМЕНТЫ ====================
        "Build" to Icons.Default.Build,
        "Handyman" to Icons.Default.Handyman,
        "Construction" to Icons.Default.Construction,
        "Hardware" to Icons.Default.Hardware,
        "HomeRepairService" to Icons.Default.HomeRepairService,
        "Carpenter" to Icons.Default.Carpenter,
        "Precision" to Icons.Default.PrecisionManufacturing,
        "Engineering" to Icons.Default.Engineering,
        "Architecture" to Icons.Default.Architecture,
        "Foundation" to Icons.Default.Foundation,
        "Roofing" to Icons.Default.Roofing,
        "Plumbing" to Icons.Default.Plumbing,
        "ElectricalServices" to Icons.Default.ElectricalServices,
        "OilBarrel" to Icons.Default.OilBarrel,
        "PropaneTank" to Icons.Default.PropaneTank,

        // ==================== КРЕПЁЖ, МЕТИЗЫ ====================
        "Settings" to Icons.Default.Settings,
        "BuildCircle" to Icons.Default.BuildCircle,
        "VpnKey" to Icons.Default.VpnKey,
        "Key" to Icons.Default.Key,
        "Lock" to Icons.Default.Lock,
        "LockOpen" to Icons.Default.LockOpen,

        // ==================== ХИМИЯ, КРАСКИ ====================
        "FormatPaint" to Icons.Default.FormatPaint,
        "ColorLens" to Icons.Default.ColorLens,
        "Brush" to Icons.Default.Brush,
        "Palette" to Icons.Default.Palette,
        "InvertColors" to Icons.Default.InvertColors,
        "WaterDrop" to Icons.Default.WaterDrop,
        "Science" to Icons.Default.Science,
        "Biotech" to Icons.Default.Biotech,
        "Chemistry" to Icons.Default.Science,

        // ==================== РАСХОДНИКИ, ХРАНЕНИЕ ====================
        "Inventory" to Icons.Default.Inventory,
        "Inventory2" to Icons.Default.Inventory2,
        "Category" to Icons.Default.Category,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Storage" to Icons.Default.Storage,
        "Archive" to Icons.Default.Archive,
        "Folder" to Icons.Default.Folder,
        "FolderSpecial" to Icons.Default.FolderSpecial,
        "FolderOpen" to Icons.Default.FolderOpen,
        "Inbox" to Icons.Default.Inbox,
        "AllInbox" to Icons.Default.AllInbox,
        "MoveToInbox" to Icons.Default.MoveToInbox,
        "Outbox" to Icons.Default.Outbox,
        "Widgets" to Icons.Default.Widgets,
        "ViewModule" to Icons.Default.ViewModule,
        "Grid4x4" to Icons.Default.Grid4x4,
        "ViewInAr" to Icons.Default.ViewInAr,

        // ==================== ИЗМЕРИТЕЛЬНЫЕ ====================
        "Straighten" to Icons.Default.Straighten,
        "Speed" to Icons.Default.Speed,
        "Timer" to Icons.Default.Timer,
        "AccessTime" to Icons.Default.AccessTime,
        "Scale" to Icons.Default.Scale,
        "Thermostat" to Icons.Default.Thermostat,
        "WbSunny" to Icons.Default.WbSunny,
        "AcUnit" to Icons.Default.AcUnit,

        // ==================== БЕЗОПАСНОСТЬ ====================
        "Security" to Icons.Default.Security,
        "Shield" to Icons.Default.Shield,
        "HealthAndSafety" to Icons.Default.HealthAndSafety,
        "Verified" to Icons.Default.Verified,
        "Warning" to Icons.Default.Warning,
        "ReportProblem" to Icons.Default.ReportProblem,
        "Dangerous" to Icons.Default.Dangerous,
        "LocalFireDepartment" to Icons.Default.LocalFireDepartment,
        "Whatshot" to Icons.Default.Whatshot,
        "FireExtinguisher" to Icons.Default.FireExtinguisher,
        "SmokeFree" to Icons.Default.SmokeFree,

        // ==================== САД, ПРИРОДА ====================
        "Grass" to Icons.Default.Grass,
        "Yard" to Icons.Default.Yard,
        "Park" to Icons.Default.Park,
        "Forest" to Icons.Default.Forest,
        "Eco" to Icons.Default.Eco,
        "LocalFlorist" to Icons.Default.LocalFlorist,
        "Agriculture" to Icons.Default.Agriculture,
        "Landscape" to Icons.Default.Landscape,
        "Terrain" to Icons.Default.Terrain,
        "Nature" to Icons.Default.Nature,
        "NaturePeople" to Icons.Default.NaturePeople,
        "EmojiNature" to Icons.Default.EmojiNature,
        "Water" to Icons.Default.Water,
        "Waves" to Icons.Default.Waves,

        // ==================== АВТО, ТРАНСПОРТ ====================
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "CarRepair" to Icons.Default.CarRepair,
        "EvStation" to Icons.Default.EvStation,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "TwoWheeler" to Icons.Default.TwoWheeler,
        "PedalBike" to Icons.Default.PedalBike,
        "LocalShipping" to Icons.Default.LocalShipping,
        "LocalShipping2" to Icons.Default.LocalShipping,
        "AirportShuttle" to Icons.Default.AirportShuttle,
        "DirectionsBoat" to Icons.Default.DirectionsBoat,
        "Flight" to Icons.Default.Flight,
        "Train" to Icons.Default.Train,

        // ==================== ЭЛЕКТРИКА, ЭНЕРГИЯ ====================
        "Bolt" to Icons.Default.Bolt,
        "Power" to Icons.Default.Power,
        "FlashOn" to Icons.Default.FlashOn,
        "BatteryFull" to Icons.Default.BatteryFull,
        "BatteryChargingFull" to Icons.Default.BatteryChargingFull,
        "ElectricMeter" to Icons.Default.ElectricMeter,
        "SolarPower" to Icons.Default.SolarPower,
        "WindPower" to Icons.Default.WindPower,
        "Lightbulb" to Icons.Default.Lightbulb,
        "Light" to Icons.Default.Light,
        "Highlight" to Icons.Default.Highlight,

        // ==================== ТЕХНИКА, ЭЛЕКТРОНИКА ====================
        "Memory" to Icons.Default.Memory,
        "Computer" to Icons.Default.Computer,
        "PhoneAndroid" to Icons.Default.PhoneAndroid,
        "Smartphone" to Icons.Default.Smartphone,
        "Cable" to Icons.Default.Cable,
        "Usb" to Icons.Default.Usb,
        "Router" to Icons.Default.Router,
        "Wifi" to Icons.Default.Wifi,
        "Speaker" to Icons.Default.Speaker,
        "Tv" to Icons.Default.Tv,
        "CameraAlt" to Icons.Default.CameraAlt,
        "Videocam" to Icons.Default.Videocam,
        "Headphones" to Icons.Default.Headphones,
        "Watch" to Icons.Default.Watch,
        "Devices" to Icons.Default.Devices,
        "SettingsRemote" to Icons.Default.SettingsRemote,
        "PowerOff" to Icons.Default.PowerOff,

        // ==================== ДОМ, СТРОИТЕЛЬСТВО ====================
        "Home" to Icons.Default.Home,
        "House" to Icons.Default.House,
        "Kitchen" to Icons.Default.Kitchen,
        "Chair" to Icons.Default.Chair,
        "TableRestaurant" to Icons.Default.TableRestaurant,
        "Bed" to Icons.Default.Bed,
        "Weekend" to Icons.Default.Weekend,
        "DoorFront" to Icons.Default.DoorFront,
        "Window" to Icons.Default.Window,
        "Balcony" to Icons.Default.Balcony,
        "Stairs" to Icons.Default.Stairs,
        "Elevator" to Icons.Default.Elevator,

        // ==================== БЫТОВАЯ ТЕХНИКА ====================
        "Microwave" to Icons.Default.Microwave,
        "Blender" to Icons.Default.Blender,
        "CoffeeMaker" to Icons.Default.CoffeeMaker,
        "LocalLaundryService" to Icons.Default.LocalLaundryService,
        "Dry" to Icons.Default.Dry,
        "Iron" to Icons.Default.Iron,
        "CleaningServices" to Icons.Default.CleaningServices,

        // ==================== ОДЕЖДА, ЗАЩИТА ====================
        "Checkroom" to Icons.Default.Checkroom,
        "DryCleaning" to Icons.Default.DryCleaning,
        "Face" to Icons.Default.Face,
        "BackHand" to Icons.Default.BackHand,

        // ==================== ЕДА, ПИТЬЁ ====================
        "Restaurant" to Icons.Default.Restaurant,
        "Fastfood" to Icons.Default.Fastfood,
        "LocalCafe" to Icons.Default.LocalCafe,
        "Coffee" to Icons.Default.Coffee,
        "LocalBar" to Icons.Default.LocalBar,
        "Liquor" to Icons.Default.Liquor,
        "WineBar" to Icons.Default.WineBar,
        "LocalPizza" to Icons.Default.LocalPizza,
        "BakeryDining" to Icons.Default.BakeryDining,

        // ==================== МЕДИЦИНА, АПТЕКА ====================
        "MedicalServices" to Icons.Default.MedicalServices,
        "LocalHospital" to Icons.Default.LocalHospital,
        "Healing" to Icons.Default.Healing,
        "Medication" to Icons.Default.Medication,
        "Vaccines" to Icons.Default.Vaccines,
        "Sick" to Icons.Default.Sick,
        "Coronavirus" to Icons.Default.Coronavirus,

        // ==================== СПОРТ ====================
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "SportsSoccer" to Icons.Default.SportsSoccer,
        "SportsBasketball" to Icons.Default.SportsBasketball,
        "SportsTennis" to Icons.Default.SportsTennis,
        "SportsMma" to Icons.Default.SportsMma,
        "SportsMartialArts" to Icons.Default.SportsMartialArts,
        "Pool" to Icons.Default.Pool,
        "DirectionsRun" to Icons.Default.DirectionsRun,
        "DirectionsBike" to Icons.Default.DirectionsBike,
        "Hiking" to Icons.Default.Hiking,
        "DownhillSkiing" to Icons.Default.DownhillSkiing,
        "Surfing" to Icons.Default.Surfing,
        "Kayaking" to Icons.Default.Kayaking,
        "Fishing" to Icons.Default.Phishing,
        "SportsEsports" to Icons.Default.SportsEsports,

        // ==================== ИНСТРУМЕНТЫ ТВОРЧЕСТВА ====================
        "MusicNote" to Icons.Default.MusicNote,
        "Piano" to Icons.Default.Piano,
        "Movie" to Icons.Default.Movie,
        "Theaters" to Icons.Default.Theaters,
        "Brush2" to Icons.Default.Brush,
        "Edit" to Icons.Default.Edit,
        "Create" to Icons.Default.Create,
        "Draw" to Icons.Default.Draw,
        "DesignServices" to Icons.Default.DesignServices,
        "AutoFixHigh" to Icons.Default.AutoFixHigh,

        // ==================== КНИГИ, ДОКУМЕНТЫ ====================
        "Book" to Icons.Default.Book,
        "MenuBook" to Icons.Default.MenuBook,
        "LibraryBooks" to Icons.Default.LibraryBooks,
        "Description" to Icons.Default.Description,
        "Article" to Icons.Default.Article,
        "PictureAsPdf" to Icons.Default.PictureAsPdf,
        "Note" to Icons.Default.Note,
        "StickyNote2" to Icons.Default.StickyNote2,

        // ==================== ВРЕМЯ, КАЛЕНДАРЬ ====================
        "CalendarToday" to Icons.Default.CalendarToday,
        "Event" to Icons.Default.Event,
        "Schedule" to Icons.Default.Schedule,
        "Alarm" to Icons.Default.Alarm,
        "HourglassEmpty" to Icons.Default.HourglassEmpty,

        // ==================== ДЕНЬГИ, ПОКУПКИ ====================
        "AttachMoney" to Icons.Default.AttachMoney,
        "Euro" to Icons.Default.Euro,
        "Savings" to Icons.Default.Savings,
        "AccountBalance" to Icons.Default.AccountBalance,
        "Receipt" to Icons.Default.Receipt,
        "LocalOffer" to Icons.Default.LocalOffer,
        "Sell" to Icons.Default.Sell,
        "Loyalty" to Icons.Default.Loyalty,
        "CardGiftcard" to Icons.Default.CardGiftcard,

        // ==================== СВЯЗЬ ====================
        "Phone" to Icons.Default.Phone,
        "Email" to Icons.Default.Email,
        "Message" to Icons.Default.Message,
        "Chat" to Icons.Default.Chat,
        "Send" to Icons.Default.Send,

        // ==================== МЕСТА, ГЕОЛОКАЦИЯ ====================
        "Place" to Icons.Default.Place,
        "LocationOn" to Icons.Default.LocationOn,
        "Map" to Icons.Default.Map,
        "Explore" to Icons.Default.Explore,
        "NearMe" to Icons.Default.NearMe,
        "MyLocation" to Icons.Default.MyLocation,
        "Public" to Icons.Default.Public,
        "Flag" to Icons.Default.Flag,

        // ==================== ЛЮДИ, ГРУППЫ ====================
        "Person" to Icons.Default.Person,
        "Group" to Icons.Default.Group,
        "People" to Icons.Default.People,
        "Engineering2" to Icons.Default.Engineering,
        "Construction2" to Icons.Default.Construction,

        // ==================== РАЗНОЕ, ОБЩЕЕ ====================
        "Star" to Icons.Default.Star,
        "StarBorder" to Icons.Default.StarBorder,
        "Favorite" to Icons.Default.Favorite,
        "FavoriteBorder" to Icons.Default.FavoriteBorder,
        "Bookmark" to Icons.Default.Bookmark,
        "BookmarkBorder" to Icons.Default.BookmarkBorder,
        "Label" to Icons.Default.Label,
        "LabelImportant" to Icons.Default.LabelImportant,
        "PushPin" to Icons.Default.PushPin,
        "Flag2" to Icons.Default.Flag,
        "EmojiObjects" to Icons.Default.EmojiObjects,
        "EmojiEvents" to Icons.Default.EmojiEvents,
        "Celebration" to Icons.Default.Celebration,
        "Rocket" to Icons.Default.Rocket,
        "Brightness1" to Icons.Default.Brightness1,
        "Circle" to Icons.Default.Circle,
        "Square" to Icons.Default.Square,
        "Hexagon" to Icons.Default.Hexagon,
        "ChangeHistory" to Icons.Default.ChangeHistory,
        "CatchingPokemon" to Icons.Default.CatchingPokemon,

        // ==================== ПРИРОДА, ПОГОДА ====================
        "Cloud" to Icons.Default.Cloud,
        "CloudQueue" to Icons.Default.CloudQueue,
        "Umbrella" to Icons.Default.Umbrella,
        "BeachAccess" to Icons.Default.BeachAccess,
        "NightsStay" to Icons.Default.NightsStay,
        "Brightness2" to Icons.Default.Brightness2,
        "Thunderstorm" to Icons.Default.Thunderstorm,
        "Tornado" to Icons.Default.Tornado,
        "Tsunami" to Icons.Default.Tsunami,
        "Volcano" to Icons.Default.Volcano,
        "Landslide" to Icons.Default.Landslide,
        "Flood" to Icons.Default.Flood,

        // ==================== ЖИВОТНЫЕ ====================
        "Pets" to Icons.Default.Pets,
        "BugReport" to Icons.Default.BugReport,
        "PestControl" to Icons.Default.PestControl,
        "EmojiNature2" to Icons.Default.EmojiNature,
    )

    /**
     * Словарь цветов по умолчанию: имя иконки → цвет.
     * Используется, если у категории нет своего цвета.
     */
    private val iconColors: Map<String, Color> = mapOf(
        // Инструменты — оранжевый
        "Build" to Color(0xFFFF8F00),
        "Handyman" to Color(0xFFFF8F00),
        "Construction" to Color(0xFFFF8F00),
        "Hardware" to Color(0xFFFF8F00),
        "HomeRepairService" to Color(0xFFFF8F00),
        "Carpenter" to Color(0xFFFF8F00),
        "PrecisionManufacturing" to Color(0xFFFF8F00),
        "Engineering" to Color(0xFFFF8F00),
        "Architecture" to Color(0xFFFF8F00),
        "Foundation" to Color(0xFF8D6E63),
        "Roofing" to Color(0xFF8D6E63),
        "Plumbing" to Color(0xFF29B6F6),
        "ElectricalServices" to Color(0xFFFFEB3B),
        "OilBarrel" to Color(0xFF795548),
        "PropaneTank" to Color(0xFFFF5722),

        // Метизы — серый
        "Settings" to Color(0xFF616161),
        "BuildCircle" to Color(0xFF616161),
        "VpnKey" to Color(0xFF616161),
        "Key" to Color(0xFFFDD835),
        "Lock" to Color(0xFF616161),
        "LockOpen" to Color(0xFF616161),

        // Химия, краски — зелёный/фиолетовый
        "FormatPaint" to Color(0xFF43A047),
        "ColorLens" to Color(0xFF8E24AA),
        "Brush" to Color(0xFF8E24AA),
        "Palette" to Color(0xFF8E24AA),
        "InvertColors" to Color(0xFF00897B),
        "WaterDrop" to Color(0xFF29B6F6),
        "Science" to Color(0xFF43A047),
        "Biotech" to Color(0xFF43A047),

        // Расходники — синий
        "Inventory" to Color(0xFF1E88E5),
        "Inventory2" to Color(0xFF1E88E5),
        "Category" to Color(0xFF1E88E5),
        "ShoppingBag" to Color(0xFF1E88E5),
        "ShoppingCart" to Color(0xFF1E88E5),
        "Storage" to Color(0xFF546E7A),
        "Archive" to Color(0xFF546E7A),
        "Folder" to Color(0xFFFB8C00),
        "FolderSpecial" to Color(0xFFFB8C00),
        "FolderOpen" to Color(0xFFFB8C00),
        "Inbox" to Color(0xFF546E7A),
        "AllInbox" to Color(0xFF546E7A),
        "MoveToInbox" to Color(0xFF546E7A),
        "Outbox" to Color(0xFF546E7A),
        "Widgets" to Color(0xFF7E57C2),
        "ViewModule" to Color(0xFF7E57C2),
        "ViewInAr" to Color(0xFF7E57C2),

        // Измерительные — голубой
        "Straighten" to Color(0xFF00ACC1),
        "Speed" to Color(0xFF00ACC1),
        "Timer" to Color(0xFF00ACC1),
        "AccessTime" to Color(0xFF00ACC1),
        "Scale" to Color(0xFF00ACC1),
        "Thermostat" to Color(0xFFEF5350),
        "WbSunny" to Color(0xFFFFCA28),
        "AcUnit" to Color(0xFF4FC3F7),

        // Безопасность — красный
        "Security" to Color(0xFFE53935),
        "Shield" to Color(0xFFE53935),
        "HealthAndSafety" to Color(0xFFE53935),
        "Verified" to Color(0xFF43A047),
        "Warning" to Color(0xFFFFB300),
        "ReportProblem" to Color(0xFFFB8C00),
        "Dangerous" to Color(0xFFD32F2F),
        "LocalFireDepartment" to Color(0xFFE53935),
        "Whatshot" to Color(0xFFFF5722),
        "FireExtinguisher" to Color(0xFFE53935),
        "SmokeFree" to Color(0xFF757575),

        // Сад — зелёный
        "Grass" to Color(0xFF4CAF50),
        "Yard" to Color(0xFF4CAF50),
        "Park" to Color(0xFF4CAF50),
        "Forest" to Color(0xFF2E7D32),
        "Eco" to Color(0xFF66BB6A),
        "LocalFlorist" to Color(0xFFE91E63),
        "Agriculture" to Color(0xFF8BC34A),
        "Landscape" to Color(0xFF7CB342),
        "Terrain" to Color(0xFF7CB342),
        "Nature" to Color(0xFF4CAF50),
        "NaturePeople" to Color(0xFF4CAF50),
        "EmojiNature" to Color(0xFF4CAF50),
        "Water" to Color(0xFF29B6F6),
        "Waves" to Color(0xFF29B6F6),

        // Авто — синий/красный
        "DirectionsCar" to Color(0xFF1976D2),
        "CarRepair" to Color(0xFF1976D2),
        "EvStation" to Color(0xFF43A047),
        "LocalGasStation" to Color(0xFFEF6C00),
        "TwoWheeler" to Color(0xFF1976D2),
        "PedalBike" to Color(0xFF1976D2),
        "LocalShipping" to Color(0xFF5D4037),
        "AirportShuttle" to Color(0xFF1976D2),
        "DirectionsBoat" to Color(0xFF0288D1),
        "Flight" to Color(0xFF1976D2),
        "Train" to Color(0xFF455A64),

        // Электрика — жёлтый
        "Bolt" to Color(0xFFFFEB3B),
        "Power" to Color(0xFFF44336),
        "FlashOn" to Color(0xFFFFEB3B),
        "BatteryFull" to Color(0xFF4CAF50),
        "BatteryChargingFull" to Color(0xFF4CAF50),
        "ElectricMeter" to Color(0xFFFFEB3B),
        "SolarPower" to Color(0xFFFFB300),
        "WindPower" to Color(0xFF00ACC1),
        "Lightbulb" to Color(0xFFFFCA28),
        "Light" to Color(0xFFFFCA28),
        "Highlight" to Color(0xFFFFCA28),

        // Техника — синий
        "Memory" to Color(0xFF1976D2),
        "Computer" to Color(0xFF1976D2),
        "PhoneAndroid" to Color(0xFF1976D2),
        "Smartphone" to Color(0xFF1976D2),
        "Cable" to Color(0xFF546E7A),
        "Usb" to Color(0xFF546E7A),
        "Router" to Color(0xFF1976D2),
        "Wifi" to Color(0xFF1976D2),
        "Speaker" to Color(0xFF546E7A),
        "Tv" to Color(0xFF212121),
        "CameraAlt" to Color(0xFF546E7A),
        "Videocam" to Color(0xFF546E7A),
        "Headphones" to Color(0xFF212121),
        "Watch" to Color(0xFF212121),
        "Devices" to Color(0xFF1976D2),

        // Дом — коричневый
        "Home" to Color(0xFF8D6E63),
        "House" to Color(0xFF8D6E63),
        "Kitchen" to Color(0xFF8D6E63),
        "Chair" to Color(0xFF8D6E63),
        "Bed" to Color(0xFF8D6E63),
        "Weekend" to Color(0xFF8D6E63),
        "DoorFront" to Color(0xFF8D6E63),
        "Window" to Color(0xFF8D6E63),
        "Balcony" to Color(0xFF8D6E63),
        "Stairs" to Color(0xFF8D6E63),

        // Бытовая техника — серый/голубой
        "Microwave" to Color(0xFF546E7A),
        "Blender" to Color(0xFF546E7A),
        "CoffeeMaker" to Color(0xFF8D6E63),
        "LocalLaundryService" to Color(0xFF1976D2),
        "Dry" to Color(0xFF1976D2),
        "Iron" to Color(0xFF1976D2),
        "CleaningServices" to Color(0xFF00ACC1),

        // Одежда — фиолетовый
        "Checkroom" to Color(0xFF7E57C2),
        "DryCleaning" to Color(0xFF7E57C2),
        "Face" to Color(0xFFFFB300),
        "BackHand" to Color(0xFFFFB300),

        // Еда — оранжевый
        "Restaurant" to Color(0xFFFF7043),
        "Fastfood" to Color(0xFFFF7043),
        "LocalCafe" to Color(0xFF8D6E63),
        "Coffee" to Color(0xFF8D6E63),
        "LocalBar" to Color(0xFFFFA726),
        "Liquor" to Color(0xFFFFA726),
        "WineBar" to Color(0xFF9C27B0),
        "LocalPizza" to Color(0xFFFF7043),
        "BakeryDining" to Color(0xFFFFB300),

        // Медицина — красный
        "MedicalServices" to Color(0xFFE53935),
        "LocalHospital" to Color(0xFFE53935),
        "Healing" to Color(0xFF43A047),
        "Medication" to Color(0xFFE53935),
        "Vaccines" to Color(0xFFE53935),
        "Sick" to Color(0xFFE53935),
        "Coronavirus" to Color(0xFFD32F2F),

        // Спорт — оранжевый
        "FitnessCenter" to Color(0xFFFF8F00),
        "SportsSoccer" to Color(0xFF4CAF50),
        "SportsBasketball" to Color(0xFFFF7043),
        "SportsTennis" to Color(0xFF8BC34A),
        "SportsMma" to Color(0xFFE53935),
        "SportsMartialArts" to Color(0xFFE53935),
        "Pool" to Color(0xFF29B6F6),
        "DirectionsRun" to Color(0xFF1976D2),
        "DirectionsBike" to Color(0xFF1976D2),
        "Hiking" to Color(0xFF4CAF50),
        "DownhillSkiing" to Color(0xFF29B6F6),
        "Surfing" to Color(0xFF29B6F6),
        "Kayaking" to Color(0xFF29B6F6),
        "Phishing" to Color(0xFF29B6F6),
        "SportsEsports" to Color(0xFF7E57C2),

        // Творчество — фиолетовый
        "MusicNote" to Color(0xFF7E57C2),
        "Piano" to Color(0xFF212121),
        "Movie" to Color(0xFF7E57C2),
        "Theaters" to Color(0xFF7E57C2),
        "Edit" to Color(0xFF1976D2),
        "Create" to Color(0xFF1976D2),
        "Draw" to Color(0xFF1976D2),
        "DesignServices" to Color(0xFF7E57C2),
        "AutoFixHigh" to Color(0xFF7E57C2),

        // Книги — синий
        "Book" to Color(0xFF1976D2),
        "MenuBook" to Color(0xFF1976D2),
        "LibraryBooks" to Color(0xFF1976D2),
        "Description" to Color(0xFF546E7A),
        "Article" to Color(0xFF546E7A),
        "PictureAsPdf" to Color(0xFFE53935),
        "Note" to Color(0xFFFFB300),
        "StickyNote2" to Color(0xFFFFB300),

        // Время — голубой
        "CalendarToday" to Color(0xFF00ACC1),
        "Event" to Color(0xFF00ACC1),
        "Schedule" to Color(0xFF00ACC1),
        "Alarm" to Color(0xFF00ACC1),
        "HourglassEmpty" to Color(0xFF00ACC1),

        // Деньги — зелёный
        "AttachMoney" to Color(0xFF4CAF50),
        "Euro" to Color(0xFF4CAF50),
        "Savings" to Color(0xFF4CAF50),
        "AccountBalance" to Color(0xFF4CAF50),
        "Receipt" to Color(0xFF4CAF50),
        "LocalOffer" to Color(0xFFE53935),
        "Sell" to Color(0xFFE53935),
        "Loyalty" to Color(0xFFE53935),
        "CardGiftcard" to Color(0xFFE53935),

        // Связь — синий
        "Phone" to Color(0xFF4CAF50),
        "Email" to Color(0xFF1976D2),
        "Message" to Color(0xFF1976D2),
        "Chat" to Color(0xFF1976D2),
        "Send" to Color(0xFF1976D2),

        // Места — красный
        "Place" to Color(0xFFE53935),
        "LocationOn" to Color(0xFFE53935),
        "Map" to Color(0xFF4CAF50),
        "Explore" to Color(0xFF4CAF50),
        "NearMe" to Color(0xFF1976D2),
        "MyLocation" to Color(0xFF1976D2),
        "Public" to Color(0xFF1976D2),
        "Flag" to Color(0xFFE53935),

        // Люди — синий
        "Person" to Color(0xFF1976D2),
        "Group" to Color(0xFF1976D2),
        "People" to Color(0xFF1976D2),

        // Разное
        "Star" to Color(0xFFFFB300),
        "StarBorder" to Color(0xFFFFB300),
        "Favorite" to Color(0xFFE91E63),
        "FavoriteBorder" to Color(0xFFE91E63),
        "Bookmark" to Color(0xFF1976D2),
        "BookmarkBorder" to Color(0xFF1976D2),
        "Label" to Color(0xFFFF7043),
        "LabelImportant" to Color(0xFFFF7043),
        "PushPin" to Color(0xFFE53935),
        "EmojiObjects" to Color(0xFFFFCA28),
        "EmojiEvents" to Color(0xFFFFB300),
        "Celebration" to Color(0xFFE91E63),
        "Rocket" to Color(0xFF7E57C2),
        "Circle" to Color(0xFF1976D2),
        "Square" to Color(0xFF1976D2),
        "Hexagon" to Color(0xFF1976D2),
        "ChangeHistory" to Color(0xFF1976D2),
        "CatchingPokemon" to Color(0xFFE53935),

        // Погода
        "Cloud" to Color(0xFF90A4AE),
        "CloudQueue" to Color(0xFF90A4AE),
        "Umbrella" to Color(0xFF1976D2),
        "BeachAccess" to Color(0xFFFFB300),
        "NightsStay" to Color(0xFF3F51B5),
        "Brightness2" to Color(0xFF3F51B5),
        "Thunderstorm" to Color(0xFF455A64),
        "Tornado" to Color(0xFF455A64),
        "Tsunami" to Color(0xFF0288D1),
        "Volcano" to Color(0xFFE53935),
        "Landslide" to Color(0xFF8D6E63),
        "Flood" to Color(0xFF29B6F6),

        // Животные
        "Pets" to Color(0xFF8D6E63),
        "BugReport" to Color(0xFF4CAF50),
        "PestControl" to Color(0xFF4CAF50),
    )

    /**
     * Получить иконку по имени. Если не найдена — Category.
     */
    fun get(name: String): ImageVector {
        return icons[name] ?: Icons.Default.Category
    }

    /**
     * Получить цвет иконки. Если не задан — цвет темы.
     */
    fun getColor(name: String): Color {
        return iconColors[name] ?: Color(0xFF1E88E5)   // синий по умолчанию
    }

    /**
     * Все имена иконок (для сетки выбора).
     */
    val allNames: List<String> get() = icons.keys.sorted()

    /**
     * Иконки по группам (для удобного выбора).
     */
    val groupedIcons: Map<String, List<String>> = mapOf(
        "Инструменты" to listOf(
            "Build", "Handyman", "Construction", "Hardware", "HomeRepairService",
            "Carpenter", "PrecisionManufacturing", "Engineering", "Architecture",
            "Foundation", "Roofing", "Plumbing", "ElectricalServices", "OilBarrel", "PropaneTank"
        ),
        "Метизы" to listOf(
            "Settings", "BuildCircle", "VpnKey", "Key", "Lock", "LockOpen"
        ),
        "Химия, краски" to listOf(
            "FormatPaint", "ColorLens", "Brush", "Palette", "InvertColors",
            "WaterDrop", "Science", "Biotech"
        ),
        "Расходники, хранение" to listOf(
            "Inventory", "Inventory2", "Category", "ShoppingBag", "ShoppingCart",
            "Storage", "Archive", "Folder", "FolderSpecial", "FolderOpen",
            "Inbox", "AllInbox", "MoveToInbox", "Outbox", "Widgets",
            "ViewModule", "ViewInAr"
        ),
        "Измерительные" to listOf(
            "Straighten", "Speed", "Timer", "AccessTime", "Scale",
            "Thermostat", "WbSunny", "AcUnit"
        ),
        "Безопасность" to listOf(
            "Security", "Shield", "HealthAndSafety", "Verified", "Warning",
            "ReportProblem", "Dangerous", "LocalFireDepartment", "Whatshot",
            "FireExtinguisher", "SmokeFree"
        ),
        "Сад, природа" to listOf(
            "Grass", "Yard", "Park", "Forest", "Eco", "LocalFlorist",
            "Agriculture", "Landscape", "Terrain", "Nature", "NaturePeople",
            "EmojiNature", "Water", "Waves"
        ),
        "Авто, транспорт" to listOf(
            "DirectionsCar", "CarRepair", "EvStation", "LocalGasStation",
            "TwoWheeler", "PedalBike", "LocalShipping", "AirportShuttle",
            "DirectionsBoat", "Flight", "Train"
        ),
        "Электрика" to listOf(
            "Bolt", "Power", "FlashOn", "BatteryFull", "BatteryChargingFull",
            "ElectricMeter", "SolarPower", "WindPower", "Lightbulb", "Light", "Highlight"
        ),
        "Техника, электроника" to listOf(
            "Memory", "Computer", "PhoneAndroid", "Smartphone", "Cable",
            "Usb", "Router", "Wifi", "Speaker", "Tv", "CameraAlt",
            "Videocam", "Headphones", "Watch", "Devices", "SettingsRemote", "PowerOff"
        ),
        "Дом, строительство" to listOf(
            "Home", "House", "Kitchen", "Chair", "TableRestaurant", "Bed",
            "Weekend", "DoorFront", "Window", "Balcony", "Stairs", "Elevator"
        ),
        "Бытовая техника" to listOf(
            "Microwave", "Blender", "CoffeeMaker", "LocalLaundryService",
            "Dry", "Iron", "CleaningServices"
        ),
        "Одежда, защита" to listOf(
            "Checkroom", "DryCleaning", "Face", "BackHand"
        ),
        "Еда, питьё" to listOf(
            "Restaurant", "Fastfood", "LocalCafe", "Coffee", "LocalBar",
            "Liquor", "WineBar", "LocalPizza", "BakeryDining"
        ),
        "Медицина" to listOf(
            "MedicalServices", "LocalHospital", "Healing", "Medication",
            "Vaccines", "Sick", "Coronavirus"
        ),
        "Спорт" to listOf(
            "FitnessCenter", "SportsSoccer", "SportsBasketball", "SportsTennis",
            "SportsMma", "SportsMartialArts", "Pool", "DirectionsRun",
            "DirectionsBike", "Hiking", "DownhillSkiing", "Surfing",
            "Kayaking", "Phishing", "SportsEsports"
        ),
        "Творчество" to listOf(
            "MusicNote", "Piano", "Movie", "Theaters", "Edit", "Create",
            "Draw", "DesignServices", "AutoFixHigh"
        ),
        "Книги, документы" to listOf(
            "Book", "MenuBook", "LibraryBooks", "Description", "Article",
            "PictureAsPdf", "Note", "StickyNote2"
        ),
        "Время" to listOf(
            "CalendarToday", "Event", "Schedule", "Alarm", "HourglassEmpty"
        ),
        "Деньги" to listOf(
            "AttachMoney", "Euro", "Savings", "AccountBalance", "Receipt",
            "LocalOffer", "Sell", "Loyalty", "CardGiftcard"
        ),
        "Связь" to listOf(
            "Phone", "Email", "Message", "Chat", "Send"
        ),
        "Места" to listOf(
            "Place", "LocationOn", "Map", "Explore", "NearMe", "MyLocation",
            "Public", "Flag"
        ),
        "Люди" to listOf(
            "Person", "Group", "People"
        ),
        "Погода" to listOf(
            "Cloud", "CloudQueue", "Umbrella", "BeachAccess", "NightsStay",
            "Brightness2", "Thunderstorm", "Tornado", "Tsunami",
            "Volcano", "Landslide", "Flood"
        ),
        "Животные" to listOf(
            "Pets", "BugReport", "PestControl"
        ),
        "Разное" to listOf(
            "Star", "StarBorder", "Favorite", "FavoriteBorder", "Bookmark",
            "BookmarkBorder", "Label", "LabelImportant", "PushPin",
            "EmojiObjects", "EmojiEvents", "Celebration", "Rocket",
            "Circle", "Square", "Hexagon", "ChangeHistory", "CatchingPokemon"
        ),
    )
}
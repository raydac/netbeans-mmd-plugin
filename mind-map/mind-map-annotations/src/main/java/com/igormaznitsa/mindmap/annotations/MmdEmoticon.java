/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.annotations;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * Set of allowed emoticons for generated MMD topics.
 *
 * @see MmdTopic#emoticon()
 */
public enum MmdEmoticon {
  /**
   * Empty value means no any icon for node.
   */
  EMPTY("empty"),
  /**
   * Icon '32_bit'
   */
  BIT_32("32_bit"),
  /**
   * Icon '3d_glasses'
   */
  GLASSES_3D("3d_glasses"),
  /**
   * Icon '64_bit'
   */
  BIT_64("64_bit"),
  /**
   * Icon 'abacus'
   */
  ABACUS("abacus"),
  /**
   * Icon 'accept_button'
   */
  ACCEPT_BUTTON("accept_button"),
  /**
   * Icon 'accept_document'
   */
  ACCEPT_DOCUMENT("accept_document"),
  /**
   * Icon 'accordion'
   */
  ACCORDION("accordion"),
  /**
   * Icon 'account_balances'
   */
  ACCOUNT_BALANCES("account_balances"),
  /**
   * Icon 'account_functions'
   */
  ACCOUNT_FUNCTIONS("account_functions"),
  /**
   * Icon 'account_menu'
   */
  ACCOUNT_MENU("account_menu"),
  /**
   * Icon 'acorn'
   */
  ACORN("acorn"),
  /**
   * Icon 'acoustic_guitar'
   */
  ACOUSTIC_GUITAR("acoustic_guitar"),
  /**
   * Icon 'action_log'
   */
  ACTION_LOG("action_log"),
  /**
   * Icon 'administrator'
   */
  ADMINISTRATOR("administrator"),
  /**
   * Icon 'agp'
   */
  AGP("agp"),
  /**
   * Icon 'anchor'
   */
  ANCHOR("anchor"),
  /**
   * Icon 'android'
   */
  ANDROID("android"),
  /**
   * Icon 'angel'
   */
  ANGEL("angel"),
  /**
   * Icon 'apple'
   */
  APPLE("apple"),
  /**
   * Icon 'application'
   */
  APPLICATION("application"),
  /**
   * Icon 'asterisk'
   */
  ASTERISK("asterisk"),
  /**
   * Icon 'atm'
   */
  ATM("atm"),
  /**
   * Icon 'attach'
   */
  ATTACH("attach"),
  /**
   * Icon 'awstats'
   */
  AWSTATS("awstats"),
  /**
   * Icon 'ax'
   */
  AX("ax"),
  /**
   * Icon 'baby_bottle'
   */
  BABY_BOTTLE("baby_bottle"),
  /**
   * Icon 'backpack'
   */
  BACKPACK("backpack"),
  /**
   * Icon 'backups'
   */
  BACKUPS("backups"),
  /**
   * Icon 'balance'
   */
  BALANCE("balance"),
  /**
   * Icon 'ballon'
   */
  BALLON("ballon"),
  /**
   * Icon 'bamboo'
   */
  BAMBOO("bamboo"),
  /**
   * Icon 'bandaid'
   */
  BANDAID("bandaid"),
  /**
   * Icon 'bandwith'
   */
  BANDWITH("bandwith"),
  /**
   * Icon 'bank'
   */
  BANK("bank"),
  /**
   * Icon 'barcode'
   */
  BARCODE("barcode"),
  /**
   * Icon 'barcode_2d'
   */
  BARCODE_2D("barcode_2d"),
  /**
   * Icon 'basket'
   */
  BASKET("basket"),
  /**
   * Icon 'baton'
   */
  BATON("baton"),
  /**
   * Icon 'battery'
   */
  BATTERY("battery"),
  /**
   * Icon 'beaker'
   */
  BEAKER("beaker"),
  /**
   * Icon 'bean'
   */
  BEAN("bean"),
  /**
   * Icon 'bed'
   */
  BED("bed"),
  /**
   * Icon 'beer'
   */
  BEER("beer"),
  /**
   * Icon 'bell'
   */
  BELL("bell"),
  /**
   * Icon 'bin'
   */
  BIN("bin"),
  /**
   * Icon 'blueprint'
   */
  BLUEPRINT("blueprint"),
  /**
   * Icon 'board_game'
   */
  BOARD_GAME("board_game"),
  /**
   * Icon 'bomb'
   */
  BOMB("bomb"),
  /**
   * Icon 'book'
   */
  BOOK("book"),
  /**
   * Icon 'bookmark'
   */
  BOOKMARK("bookmark"),
  /**
   * Icon 'books'
   */
  BOOKS("books"),
  /**
   * Icon 'bookshelf'
   */
  BOOKSHELF("bookshelf"),
  /**
   * Icon 'boomerang'
   */
  BOOMERANG("boomerang"),
  /**
   * Icon 'bow'
   */
  BOW("bow"),
  /**
   * Icon 'box'
   */
  BOX("box"),
  /**
   * Icon 'brain'
   */
  BRAIN("brain"),
  /**
   * Icon 'bug'
   */
  BUG("bug"),
  /**
   * Icon 'burro'
   */
  BURRO("burro"),
  /**
   * Icon 'bus'
   */
  BUS("bus"),
  /**
   * Icon 'butterfly'
   */
  BUTTERFLY("butterfly"),
  /**
   * Icon 'building'
   */
  BUILDING("building"),
  /**
   * Icon 'cactus'
   */
  CACTUS("cactus"),
  /**
   * Icon 'cake'
   */
  CAKE("cake"),
  /**
   * Icon 'cancel'
   */
  CANCEL("cancel"),
  /**
   * Icon 'car'
   */
  CAR("car"),
  /**
   * Icon 'cart'
   */
  CART("cart"),
  /**
   * Icon 'cat'
   */
  CAT("cat"),
  /**
   * Icon 'caution_biohazard'
   */
  CAUTION_BIOHAZARD("caution_biohazard"),
  /**
   * Icon 'ceo'
   */
  CEO("ceo"),
  /**
   * Icon 'chameleon'
   */
  CHAMELEON("chameleon"),
  /**
   * Icon 'chart'
   */
  CHART("chart"),
  /**
   * Icon 'cigarette'
   */
  CIGARETTE("cigarette"),
  /**
   * Icon 'cinema_ticket'
   */
  CINEMA_TICKET("cinema_ticket"),
  /**
   * Icon 'class_module'
   */
  CLASS_MODULE("class_module"),
  /**
   * Icon 'clock'
   */
  CLOCK("clock"),
  /**
   * Icon 'clown_fish'
   */
  CLOWN_FISH("clown_fish"),
  /**
   * Icon 'co2'
   */
  CO2("co2"),
  /**
   * Icon 'cocacola'
   */
  COCACOLA("cocacola"),
  /**
   * Icon 'code'
   */
  CODE("code"),
  /**
   * Icon 'cog'
   */
  COG("cog"),
  /**
   * Icon 'coins'
   */
  COINS("coins"),
  /**
   * Icon 'comment'
   */
  COMMENT("comment"),
  /**
   * Icon 'compass'
   */
  COMPASS("compass"),
  /**
   * Icon 'compile'
   */
  COMPILE("compile"),
  /**
   * Icon 'compress'
   */
  COMPRESS("compress"),
  /**
   * Icon 'computer'
   */
  COMPUTER("computer"),
  /**
   * Icon 'connect'
   */
  CONNECT("connect"),
  /**
   * Icon 'construction'
   */
  CONSTRUCTION("construction"),
  /**
   * Icon 'cookies'
   */
  COOKIES("cookies"),
  /**
   * Icon 'cooler'
   */
  COOLER("cooler"),
  /**
   * Icon 'cruise_ship'
   */
  CRUISE_SHIP("cruise_ship"),
  /**
   * Icon 'cup'
   */
  CUP("cup"),
  /**
   * Icon 'curriculum_vitae'
   */
  CURRICULUM_VITAE("curriculum_vitae"),
  /**
   * Icon 'dashboard'
   */
  DASHBOARD("dashboard"),
  /**
   * Icon 'database'
   */
  DATABASE("database"),
  /**
   * Icon 'do_not_disturb'
   */
  DO_NOT_DISTURB("do_not_disturb"),
  /**
   * Icon 'draft'
   */
  DRAFT("draft"),
  /**
   * Icon 'dynamite'
   */
  DYNAMITE("dynamite"),
  /**
   * Icon 'egyptian_pyramid'
   */
  EGYPTIAN_PYRAMID("egyptian_pyramid"),
  /**
   * Icon 'email'
   */
  EMAIL("email"),
  /**
   * Icon 'emotion_adore'
   */
  EMOTION_ADORE("emotion_adore"),
  /**
   * Icon 'emotion_amazing'
   */
  EMOTION_AMAZING("emotion_amazing"),
  /**
   * Icon 'emotion_anger'
   */
  EMOTION_ANGER("emotion_anger"),
  /**
   * Icon 'emotion_angry'
   */
  EMOTION_ANGRY("emotion_angry"),
  /**
   * Icon 'emotion_bad_egg'
   */
  EMOTION_BAD_EGG("emotion_bad_egg"),
  /**
   * Icon 'emotion_bad_smelly'
   */
  EMOTION_BAD_SMELLY("emotion_bad_smelly"),
  /**
   * Icon 'emotion_baffle'
   */
  EMOTION_BAFFLE("emotion_baffle"),
  /**
   * Icon 'emotion_beaten'
   */
  EMOTION_BEATEN("emotion_beaten"),
  /**
   * Icon 'emotion_bigsmile'
   */
  EMOTION_BIGSMILE("emotion_bigsmile"),
  /**
   * Icon 'emotion_blind'
   */
  EMOTION_BLIND("emotion_blind"),
  /**
   * Icon 'emotion_bloody'
   */
  EMOTION_BLOODY("emotion_bloody"),
  /**
   * Icon 'emotion_clown'
   */
  EMOTION_CLOWN("emotion_clown"),
  /**
   * Icon 'emotion_cry'
   */
  EMOTION_CRY("emotion_cry"),
  /**
   * Icon 'emotion_dribble'
   */
  EMOTION_DRIBBLE("emotion_dribble"),
  /**
   * Icon 'emotion_ghost'
   */
  EMOTION_GHOST("emotion_ghost"),
  /**
   * Icon 'emotion_injured'
   */
  EMOTION_INJURED("emotion_injured"),
  /**
   * Icon 'emotion_kiss'
   */
  EMOTION_KISS("emotion_kiss"),
  /**
   * Icon 'emotion_lol'
   */
  EMOTION_LOL("emotion_lol"),
  /**
   * Icon 'emotion_mad'
   */
  EMOTION_MAD("emotion_mad"),
  /**
   * Icon 'emotion_medic'
   */
  EMOTION_MEDIC("emotion_medic"),
  /**
   * Icon 'emotion_nerd'
   */
  EMOTION_NERD("emotion_nerd"),
  /**
   * Icon 'emotion_sick'
   */
  EMOTION_SICK("emotion_sick"),
  /**
   * Icon 'entity'
   */
  ENTITY("entity"),
  /**
   * Icon 'envelope'
   */
  ENVELOPE("envelope"),
  /**
   * Icon 'error'
   */
  ERROR("error"),
  /**
   * Icon 'exclamation'
   */
  EXCLAMATION("exclamation"),
  /**
   * Icon 'eye'
   */
  EYE("eye"),
  /**
   * Icon 'factory'
   */
  FACTORY("factory"),
  /**
   * Icon 'film'
   */
  FILM("film"),
  /**
   * Icon 'filter'
   */
  FILTER("filter"),
  /**
   * Icon 'find'
   */
  FIND("find"),
  /**
   * Icon 'fire'
   */
  FIRE("fire"),
  /**
   * Icon 'flag_red_cross'
   */
  FLAG_RED_CROSS("flag_red_cross"),
  /**
   * Icon 'flamingo'
   */
  FLAMINGO("flamingo"),
  /**
   * Icon 'flashdisk'
   */
  FLASHDISK("flashdisk"),
  /**
   * Icon 'flask'
   */
  FLASK("flask"),
  /**
   * Icon 'flood_it'
   */
  FLOOD_IT("flood_it"),
  /**
   * Icon 'flower'
   */
  FLOWER("flower"),
  /**
   * Icon 'fog'
   */
  FOG("fog"),
  /**
   * Icon 'forklift'
   */
  FORKLIFT("forklift"),
  /**
   * Icon 'form'
   */
  FORM("form"),
  /**
   * Icon 'fruit_grape'
   */
  FRUIT_GRAPE("fruit_grape"),
  /**
   * Icon 'fruit_lime'
   */
  FRUIT_LIME("fruit_lime"),
  /**
   * Icon 'ftp'
   */
  FTP("ftp"),
  /**
   * Icon 'gas'
   */
  GAS("gas"),
  /**
   * Icon 'gear_in'
   */
  GEAR_IN("gear_in"),
  /**
   * Icon 'global_telecom'
   */
  GLOBAL_TELECOM("global_telecom"),
  /**
   * Icon 'globe_model'
   */
  GLOBE_MODEL("globe_model"),
  /**
   * Icon 'grass'
   */
  GRASS("grass"),
  /**
   * Icon 'green'
   */
  GREEN("green"),
  /**
   * Icon 'green_wormhole'
   */
  GREEN_WORMHOLE("green_wormhole"),
  /**
   * Icon 'green_yellow'
   */
  GREEN_YELLOW("green_yellow"),
  /**
   * Icoon 'grenade'
   */
  GRENADE("grenade"),
  /**
   * Icon 'group'
   */
  GROUP("group"),
  /**
   * Icon 'gun'
   */
  GUN("gun"),
  /**
   * Icon 'hand'
   */
  HAND("hand"),
  /**
   * Icon 'handbag'
   */
  HANDBAG("handbag"),
  /**
   * Icon 'hand_point'
   */
  HAND_POINT("hand_point"),
  /**
   * Icon 'hat'
   */
  HAT("hat"),
  /**
   * Icon 'headphone'
   */
  HEADPHONE("headphone"),
  /**
   * Icon 'headphone_mic'
   */
  HEADPHONE_MIC("headphone_mic"),
  /**
   * Icon 'health'
   */
  HEALTH("health"),
  /**
   * Icon 'heart'
   */
  HEART("heart"),
  /**
   * Icon 'helicopter'
   */
  HELICOPTER("helicopter"),
  /**
   * Icon 'helmet'
   */
  HELMET("helmet"),
  /**
   * Icon 'help'
   */
  HELP("help"),
  /**
   * Icon 'highlighter'
   */
  HIGHLIGHTER("highlighter"),
  /**
   * Icon 'hippocampus'
   */
  HIPPOCAMPUS("hippocampus"),
  /**
   * Icon 'holly'
   */
  HOLLY("holly"),
  /**
   * Icon 'home_page'
   */
  HOME_PAGE("home_page"),
  /**
   * Icon 'horn'
   */
  HORN("horn"),
  /**
   * Icon 'horoscopes'
   */
  HOROSCOPES("horoscopes"),
  /**
   * Icon 'hospital'
   */
  HOSPITAL("hospital"),
  /**
   * Icon 'hot'
   */
  HOT("hot"),
  /**
   * Icon 'hotjobs'
   */
  HOTJOBS("hotjobs"),
  /**
   * Icon 'hourglass'
   */
  HOURGLASS("hourglass"),
  /**
   * Icon 'house'
   */
  HOUSE("house"),
  /**
   * Icon 'hummingbird'
   */
  HUMMINGBIRD("hummingbird"),
  /**
   * Icon 'icecream'
   */
  ICECREAM("icecream"),
  /**
   * Icon 'images'
   */
  IMAGES("images"),
  /**
   * Icon 'infocard'
   */
  INFOCARD("infocard"),
  /**
   * Icon 'information'
   */
  INFORMATION("information"),
  /**
   * Icon 'injection'
   */
  INJECTION("injection"),
  /**
   * Icon 'installer_box'
   */
  INSTALLER_BOX("installer_box"),
  /**
   * Icon 'ipad'
   */
  IPAD("ipad"),
  /**
   * Icon 'ipod'
   */
  IPOD("ipod"),
  /**
   * Icon 'jacket'
   */
  JACKET("jacket"),
  /**
   * Icon 'jeans'
   */
  JEANS("jeans"),
  /**
   * Icon 'joystick'
   */
  JOYSTICK("joystick"),
  /**
   * Icon 'key'
   */
  KEY("key"),
  /**
   * Icon 'keyboard'
   */
  KEYBOARD("keyboard"),
  /**
   * Icon 'kids'
   */
  KIDS("kids"),
  /**
   * Icon 'knot'
   */
  KNOT("knot"),
  /**
   * Icon 'ladybird'
   */
  LADYBIRD("ladybird"),
  /**
   * Icon 'landmarks'
   */
  LANDMARKS("landmarks"),
  /**
   * Icon 'laptop'
   */
  LAPTOP("laptop"),
  /**
   * Icon 'leaf_plant'
   */
  LEAF_PLANT("leaf_plant"),
  /**
   * Icon 'led'
   */
  LED("led"),
  /**
   * Icon 'legend'
   */
  LEGEND("legend"),
  /**
   * Icon 'lightbulb'
   */
  LIGHTBULB("lightbulb"),
  /**
   * Icon 'lightbulb_off'
   */
  LIGHTBULB_OFF("lightbulb_off"),
  /**
   * Icon 'lightning'
   */
  LIGHTNING("lightning"),
  /**
   * Icon 'lock'
   */
  LOCK("lock"),
  /**
   * Icon 'lorry'
   */
  LORRY("lorry"),
  /**
   * Icon 'luggage'
   */
  LUGGAGE("luggage"),
  /**
   * Icon 'magnet'
   */
  MAGNET("magnet"),
  /**
   * Icon 'map'
   */
  MAP("map"),
  /**
   * Icon 'mario'
   */
  MARIO("mario"),
  /**
   * Icon 'mask'
   */
  MASK("mask"),
  /**
   * Icon 'measure'
   */
  MEASURE("measure"),
  /**
   * Icon 'medical_record'
   */
  MEDICAL_RECORD("medical_record"),
  /**
   * Icon 'menu'
   */
  MENU("menu"),
  /**
   * Icon 'metro'
   */
  METRO("metro"),
  /**
   * Icon 'module'
   */
  MODULE("module"),
  /**
   * Icon 'molecule'
   */
  MOLECULE("molecule"),
  /**
   * Icon 'money'
   */
  MONEY("money"),
  /**
   * Icon 'monitor'
   */
  MONITOR("monitor"),
  /**
   * Icon 'mouse'
   */
  MOUSE("mouse"),
  /**
   * Icon 'movies'
   */
  MOVIES("movies"),
  /**
   * Icon 'multitool'
   */
  MULTITOOL("multitool"),
  /**
   * Icon 'music'
   */
  MUSIC("music"),
  /**
   * Icon 'mustache'
   */
  MUSTACHE("mustache"),
  /**
   * Icon 'new'
   */
  NEW("new"),
  /**
   * Icon 'oil'
   */
  OIL("oil"),
  /**
   * Icon 'omelet'
   */
  OMELET("omelet"),
  /**
   * Icon 'organisation'
   */
  ORGANISATION("organisation"),
  /**
   * Icon 'origami'
   */
  ORIGAMI("origami"),
  /**
   * Icon 'page'
   */
  PAGE("page"),
  /**
   * Icon 'parrot'
   */
  PARROT("parrot"),
  /**
   * Icon 'peacock'
   */
  PEACOCK("peacock"),
  /**
   * Icon 'peak_cap'
   */
  PEAK_CAP("peak_cap"),
  /**
   * Icon 'pearl'
   */
  PEARL("pearl"),
  /**
   * Icon 'pencil'
   */
  PENCIL("pencil"),
  /**
   * Icon 'pepper'
   */
  PEPPER("pepper"),
  /**
   * Icon 'perfomance'
   */
  PERFOMANCE("perfomance"),
  /**
   * Icon 'phone'
   */
  PHONE("phone"),
  /**
   * Icon 'photo'
   */
  PHOTO("photo"),
  /**
   * Icon 'photos'
   */
  PHOTOS("photos"),
  /**
   * Icon 'piano'
   */
  PIANO("piano"),
  /**
   * Icon 'picture'
   */
  PICTURE("picture"),
  /**
   * Icon 'piece_of_cake'
   */
  PIECE_OF_CAKE("piece_of_cake"),
  /**
   * Icon 'pill'
   */
  PILL("pill"),
  /**
   * Icon 'pint'
   */
  PINT("pint"),
  /**
   * Icon 'pizza'
   */
  PIZZA("pizza"),
  /**
   * Icon 'plane'
   */
  PLANE("plane"),
  /**
   * Icon 'plant'
   */
  PLANT("plant"),
  /**
   * Icon 'playing_cards'
   */
  PLAYING_CARDS("playing_cards"),
  /**
   * Icon 'plugin'
   */
  PLUGIN("plugin"),
  /**
   * Icon 'poker'
   */
  POKER("poker"),
  /**
   * Icon 'poo'
   */
  POO("poo"),
  /**
   * Icon 'popcorn'
   */
  POPCORN("popcorn"),
  /**
   * Icon 'port'
   */
  PORT("port"),
  /**
   * Icon 'printer'
   */
  PRINTER("printer"),
  /**
   * Icon 'private'
   */
  PRIVATE("private"),
  /**
   * Icon 'processor'
   */
  PROCESSOR("processor"),
  /**
   * Icon 'quill'
   */
  QUILL("quill"),
  /**
   * Icon 'rabbit'
   */
  RABBIT("rabbit"),
  /**
   * Icon 'radiolocator'
   */
  RADIOLOCATOR("radiolocator"),
  /**
   * Icon 'rain'
   */
  RAIN("rain"),
  /**
   * Icon 'rainbow'
   */
  RAINBOW("rainbow"),
  /**
   * Icon 'receipt'
   */
  RECEIPT("receipt"),
  /**
   * Icon 'relationships'
   */
  RELATIONSHIPS("relationships"),
  /**
   * Icon 'remote'
   */
  REMOTE("remote"),
  /**
   * Icon 'report'
   */
  REPORT("report"),
  /**
   * Icon 'research'
   */
  RESEARCH("research"),
  /**
   * Icon 'resources'
   */
  RESOURCES("resources"),
  /**
   * Icon 'ring'
   */
  RING("ring"),
  /**
   * Icon 'rip'
   */
  RIP("rip"),
  /**
   * Icon 'roadworks'
   */
  ROADWORKS("roadworks"),
  /**
   * Icon 'robot'
   */
  ROBOT("robot"),
  /**
   * Icon 'rocket'
   */
  ROCKET("rocket"),
  /**
   * Icon 'role'
   */
  ROLE("role"),
  /**
   * Icon 'rosette'
   */
  ROSETTE("rosette"),
  /**
   * Icon 'router'
   */
  ROUTER("router"),
  /**
   * Icon 'rubber_duck'
   */
  RUBBER_DUCK("rubber_duck"),
  /**
   * Icon 'ruby'
   */
  RUBY("ruby"),
  /**
   * Icon 'safe'
   */
  SAFE("safe"),
  /**
   * Icon 'salver'
   */
  SALVER("salver"),
  /**
   * Icon 'santa'
   */
  SANTA("santa"),
  /**
   * Icon 'satellite'
   */
  SATELLITE("satellite"),
  /**
   * Icon 'satellite_dish'
   */
  SATELLITE_DISH("satellite_dish"),
  /**
   * Icon 'script'
   */
  SCRIPT("script"),
  /**
   * Icon 'certificate', it has typo in ID as 'sertificate' but it kept for back compatibility
   *
   * @since 1.6.1
   */
  CERTIFICATE("sertificate"),
  /**
   * Icon 'server'
   */
  SERVER("server"),
  /**
   * Icon 'server_component'
   */
  SERVER_COMPONENTS("server_components"),
  /**
   * Icon 'shoe'
   */
  SHOE("shoe"),
  /**
   * Icon 'shop'
   */
  SHOP("shop"),
  /**
   * Icon 'skull_old'
   */
  SKULL_OLD("skull_old"),
  /**
   * Icon 'snail'
   */
  SNAIL("snail"),
  /**
   * Icon 'snake_and_cup'
   */
  SNAKE_AND_CUP("snake_and_cup"),
  /**
   * Icon 'sneakers'
   */
  SNEAKERS("sneakers"),
  /**
   * Icon 'snowman'
   */
  SNOWMAN("snowman"),
  /**
   * Icon 'snowman_head'
   */
  SNOWMAN_HEAD("snowman_head"),
  /**
   * Icon 'snow_rain'
   */
  SNOW_RAIN("snow_rain"),
  /**
   * Icon 'sofa'
   */
  SOFA("sofa"),
  /**
   * Icon 'sound'
   */
  SOUND("sound"),
  /**
   * Icon 'soup'
   */
  SOUP("soup"),
  /**
   * Icon 'source_code'
   */
  SOURCE_CODE("source_code"),
  /**
   * Icon 'spam'
   */
  SPAM("spam"),
  /**
   * Icon 'speedometer'
   */
  SPEEDOMETER("speedometer"),
  /**
   * Icon 'sport'
   */
  SPORT("sport"),
  /**
   * Icon 'spray'
   */
  SPRAY("spray"),
  /**
   * Icon 'spring'
   */
  SPRING("spring"),
  /**
   * Icon 'sql'
   */
  SQL("sql"),
  /**
   * Icon 'stairs'
   */
  STAIRS("stairs"),
  /**
   * Icon 'star'
   */
  STAR("star"),
  /**
   * Icon 'status_busy'
   */
  STATUS_BUSY("status_busy"),
  /**
   * Icon 'status_offline'
   */
  STATUS_OFFLINE("status_offline"),
  /**
   * Icon 'status_online'
   */
  STATUS_ONLINE("status_online"),
  /**
   * Icon 'steering_wheel'
   */
  STEERING_WHEEL("steering_wheel"),
  /**
   * Icon 'stethoscope'
   */
  STETHOSCOPE("stethoscope"),
  /**
   * Icon 'stop'
   */
  STOP("stop"),
  /**
   * Icon 'storage'
   */
  STORAGE("storage"),
  /**
   * Icon 'support'
   */
  SUPPORT("support"),
  /**
   * Icon 'sushi'
   */
  SUSHI("sushi"),
  /**
   * Icon 'sword'
   */
  SWORD("sword"),
  /**
   * Icon 'table'
   */
  TABLE("table"),
  /**
   * Icon 'tablets'
   */
  TABLETS("tablets"),
  /**
   * Icon 'tag'
   */
  TAG("tag"),
  /**
   * Icon 'teapot'
   */
  TEAPOT("teapot"),
  /**
   * Icon 'teddy_bear'
   */
  TEDDY_BEAR("teddy_bear"),
  /**
   * Icon 'telephone'
   */
  TELEPHONE("telephone"),
  /**
   * Icon 'terminal'
   */
  TERMINAL("terminal"),
  /**
   * Icon 'text'
   */
  TEXT("text"),
  /**
   * Icon 'theater'
   */
  THEATER("theater"),
  /**
   * Icon 'tick'
   */
  TICK("tick"),
  /**
   * Icon 'tie'
   */
  TIE("tie"),
  /**
   * Icon 'time'
   */
  TIME("time"),
  /**
   * Icon 'tipper'
   */
  TIPPER("tipper"),
  /**
   * Icon 'tire'
   */
  TIRE("tire"),
  /**
   * Icon 'todo_list'
   */
  TODO_LIST("todo_list"),
  /**
   * Icon 'toilet_pan'
   */
  TOILET_PAN("toilet_pan"),
  /**
   * Icon 'tooth'
   */
  TOOTH("tooth"),
  /**
   * Icon 'tornado'
   */
  TORNADO("tornado"),
  /**
   * Icon 'toucan'
   */
  TOUCAN("toucan"),
  /**
   * Icon 'tower'
   */
  TOWER("tower"),
  /**
   * Icon 'toxic'
   */
  TOXIC("toxic"),
  /**
   * Icon 'tractor'
   */
  TRACTOR("tractor"),
  /**
   * Icon 'trade'
   */
  TRADE("trade"),
  /**
   * Icon 'traffic_lights'
   */
  TRAFFIC_LIGHTS("traffic_lights"),
  /**
   * Icon 'train'
   */
  TRAIN("train"),
  /**
   * Icon 'transmit'
   */
  TRANSMIT("transmit"),
  /**
   * Icon 'travel'
   */
  TRAVEL("travel"),
  /**
   * Icon 'tree'
   */
  TREE("tree"),
  /**
   * Icon 'tree_bare'
   */
  TREE_BARE("tree_bare"),
  /**
   * Icon 'trojan_horse'
   */
  TROJAN_HORSE("trojan_horse"),
  /**
   * Icon 'trolley'
   */
  TROLLEY("trolley"),
  /**
   * Icon 'tub'
   */
  TUB("tub"),
  /**
   * Icon 'tux'
   */
  TUX("tux"),
  /**
   * Icon 'tv'
   */
  TV("tv"),
  /**
   * Icon 'ubuntu'
   */
  UBUNTU("ubuntu"),
  /**
   * Icon 'ufo'
   */
  UFO("ufo"),
  /**
   * Icon 'umbrella'
   */
  UMBRELLA("umbrella"),
  /**
   * Icon 'unicorn'
   */
  UNICORN("unicorn"),
  /**
   * Icon 'universal_binary'
   */
  UNIVERSAL_BINARY("universal_binary"),
  /**
   * Icon 'update'
   */
  UPDATE("update"),
  /**
   * Icon 'user'
   */
  USER("user"),
  /**
   * Icon 'user_bart'
   */
  USER_BART("user_bart"),
  /**
   * Icon 'user_batman'
   */
  USER_BATMAN("user_batman"),
  /**
   * Icon 'user_bender'
   */
  USER_BENDER("user_bender"),
  /**
   * Icon 'user_c3po'
   */
  USER_C3PO("user_c3po"),
  /**
   * Icon 'user_catwomen'
   */
  USER_CATWOMEN("user_catwomen"),
  /**
   * Icon 'user_clown'
   */
  USER_CLOWN("user_clown"),
  /**
   * Icon 'user_darth_vader'
   */
  USER_DARTH_VADER("user_darth_vader"),
  /**
   * Icon 'user_death'
   */
  USER_DEATH("user_death"),
  /**
   * Icon 'user_devil'
   */
  USER_DEVIL("user_devil"),
  /**
   * Icon 'user_dracula'
   */
  USER_DRACULA("user_dracula"),
  /**
   * Icon 'user_female'
   */
  USER_FEMALE("user_female"),
  /**
   * Icon 'user_freddy'
   */
  USER_FREDDY("user_freddy"),
  /**
   * Icon 'user_gladiator'
   */
  USER_GLADIATOR("user_gladiator"),
  /**
   * Icon 'user_gomer'
   */
  USER_GOMER("user_gomer"),
  /**
   * Icon 'user_halk'
   */
  USER_HALK("user_halk"),
  /**
   * Icon 'user_ironman'
   */
  USER_IRONMAN("user_ironman"),
  /**
   * Icon 'user_ninja'
   */
  USER_NINJA("user_ninja"),
  /**
   * Icon 'user_officer'
   */
  USER_OFFICER("user_officer"),
  /**
   * Icon 'user_pilot'
   */
  USER_PILOT("user_pilot"),
  /**
   * Icon 'user_pirate'
   */
  USER_PIRATE("user_pirate"),
  /**
   * Icon 'user_r2d2'
   */
  USER_R2D2("user_r2d2"),
  /**
   * Icon 'user_robocop'
   */
  USER_ROBOCOP("user_robocop"),
  /**
   * Icon 'user_samurai'
   */
  USER_SAMURAI("user_samurai"),
  /**
   * Icon 'users_men_women'
   */
  USERS_MEN_WOMEN("users_men_women"),
  /**
   * Icon 'user_sponge_bob'
   */
  USER_SPONGE_BOB("user_sponge_bob"),
  /**
   * Icon 'user_superman'
   */
  USER_SUPERMAN("user_superman"),
  /**
   * Icon 'user_trooper'
   */
  USER_TROOPER("user_trooper"),
  /**
   * Icon 'user_viking'
   */
  USER_VIKING("user_viking"),
  /**
   * Icon 'user_yoda'
   */
  USER_YODA("user_yoda"),
  /**
   * Icon 'vase'
   */
  VASE("vase"),
  /**
   * Icon 'vcard'
   */
  VCARD("vcard"),
  /**
   * Icon 'video'
   */
  VIDEO("video"),
  /**
   * Icon 'virus_protection'
   */
  VIRUS_PROTECTION("virus_protection"),
  /**
   * Icon 'vlc'
   */
  VLC("vlc"),
  /**
   * Icon 'walk'
   */
  WALK("walk"),
  /**
   * Icon 'wall'
   */
  WALL("wall"),
  /**
   * Icon 'wall_breack'
   */
  WALL_BREACK("wall_breack"),
  /**
   * Icon 'wallet'
   */
  WALLET("wallet"),
  /**
   * Icon 'wand'
   */
  WAND("wand"),
  /**
   * Icon 'warning'
   */
  WARNING("warning"),
  /**
   * Icon 'widgets'
   */
  WIDGETS("widgets"),
  /**
   * Icon 'windy'
   */
  WINDY("windy"),
  /**
   * Icon 'wizard'
   */
  WIZARD("wizard"),
  /**
   * Icon 'world'
   */
  WORLD("world"),
  /**
   * Icon 'wrench'
   */
  WRENCH("wrench"),
  /**
   * Icon 'www'
   */
  WWW("www"),
  /**
   * Icon 'xfn'
   */
  XFN("xfn"),
  /**
   * Icon 'yacht'
   */
  YACHT("yacht"),
  /**
   * Icon 'zone'
   */
  ZONE("zone"),
  /**
   * Icon 'zoom'
   */
  ZOOM("zoom");

  private final String id;

  private static final List<MmdEmoticon> LIST_VALUES =
      stream(MmdEmoticon.values()).collect(toList());

  /**
   * Get all values as immutable list.
   *
   * @return immutable list of all values.
   * @since 1.6.6
   */
  public static List<MmdEmoticon> asList() {
    return LIST_VALUES;
  }

  /**
   * Safe case-insensitive emoticon search for name.
   *
   * @param name            emoticon name, can be null
   * @param defaultEmoticon default emoticon, can be null
   * @return found emoticon for name or the default one, the default one can be null
   * @since 1.6.6
   */
  public static MmdEmoticon findForName(final String name, final MmdEmoticon defaultEmoticon) {
    if (name == null) {
      return defaultEmoticon;
    }
    return LIST_VALUES.stream().filter(x -> x.name().equalsIgnoreCase(name)).findFirst()
        .orElse(defaultEmoticon);
  }


  MmdEmoticon(final String id) {
    this.id = id;
  }

  /**
   * Get ID as string value.
   *
   * @return id as string name
   */
  public String getId() {
    return this.id;
  }
}

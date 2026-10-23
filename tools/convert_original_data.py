#!/usr/bin/env python3
"""
Convertit les données d'Ancient Warfare 2 (1.12.2) vers AncientWarfareRecompiled (1.21.1).

Usage : python3 tools/convert_original_data.py <dossier assets/ancientwarfare> <dossier de sortie>

Sorties (dans <dossier de sortie>) :
  recipe/            recettes de craft converties (chargées par le jeu)
  research/          objectifs de recherche convertis (chargés par le jeu)
  recipe/research/   recettes de recherche : recettes de craft avec le champ "research" (verrouillées à l'établi)
  tags/item/         tags de l'objet du mod (engrenages, arbres, lingots d'acier)
  conversion_report.txt
"""
import json, glob, os, sys, collections

SRC, OUT = sys.argv[1], sys.argv[2]
NS = "ancientwarfarerecompiled"
# Objets d'autres modules déjà portés : identifiant d'origine -> identifiant recompilé
PORTED = {
    "ancientwarfareautomation:chunk_loader_simple": f"{NS}:chunk_loader_simple",
    "ancientwarfareautomation:chunk_loader_deluxe": f"{NS}:chunk_loader_deluxe",
    "ancientwarfareautomation:mailbox": f"{NS}:mailbox",
    "ancientwarfareautomation:hand_cranked_generator": f"{NS}:hand_cranked_generator",
    "ancientwarfareautomation:stirling_generator": f"{NS}:stirling_generator",
    "ancientwarfareautomation:torque_distributor_light": f"{NS}:torque_distributor_light",
    "ancientwarfareautomation:torque_distributor_medium": f"{NS}:torque_distributor_medium",
    "ancientwarfareautomation:torque_distributor_heavy": f"{NS}:torque_distributor_heavy",
    "ancientwarfareautomation:torque_junction_light": f"{NS}:torque_junction_light",
    "ancientwarfareautomation:torque_junction_medium": f"{NS}:torque_junction_medium",
    "ancientwarfareautomation:torque_junction_heavy": f"{NS}:torque_junction_heavy",
    "ancientwarfareautomation:windmill_generator": f"{NS}:windmill_generator",
    "ancientwarfareautomation:windmill_blade": f"{NS}:windmill_blade",
}

# Objets du mod d'origine -> objets portés (seuls ceux qui existent déjà dans le projet)
AW_ITEMS = {
    "ancientwarfare:steel_ingot": f"{NS}:steel_ingot",
    "ancientwarfare:research_station": f"{NS}:research_station",
    "ancientwarfare:engineering_station": f"{NS}:engineering_station",
    "ancientwarfare:research_book": f"{NS}:research_book",
    "ancientwarfare:wooden_hammer": f"{NS}:wooden_hammer",
    "ancientwarfare:stone_hammer": f"{NS}:stone_hammer",
    "ancientwarfare:iron_hammer": f"{NS}:iron_hammer",
    "ancientwarfare:gold_hammer": f"{NS}:gold_hammer",
    "ancientwarfare:diamond_hammer": f"{NS}:diamond_hammer",
    "ancientwarfare:wooden_quill": f"{NS}:wooden_quill",
    "ancientwarfare:stone_quill": f"{NS}:stone_quill",
    "ancientwarfare:iron_quill": f"{NS}:iron_quill",
    "ancientwarfare:gold_quill": f"{NS}:gold_quill",
    "ancientwarfare:diamond_quill": f"{NS}:diamond_quill",
    "ancientwarfare:wooden_gear": f"{NS}:wooden_gear_set",
    "ancientwarfare:iron_gear": f"{NS}:iron_gear_set",
    "ancientwarfare:steel_gear": f"{NS}:steel_gear_set",
    "ancientwarfare:wooden_bearings": f"{NS}:wooden_bearings",
    "ancientwarfare:iron_bearings": f"{NS}:iron_bearings",
    "ancientwarfare:steel_bearings": f"{NS}:steel_bearings",
    "ancientwarfare:wooden_shaft": f"{NS}:wooden_torque_shaft",
    "ancientwarfare:iron_shaft": f"{NS}:iron_torque_shaft",
    "ancientwarfare:steel_shaft": f"{NS}:steel_torque_shaft",
}

# Tags oredict / #TAG d'origine -> tags NeoForge (c:) ou vanille, ou tags du mod (définis plus bas)
TAGS = {
    "#PLANKWOOD": "#minecraft:planks",
    "#CHEST": "#c:chests/wooden",
    "#COBBLESTONE": "#c:cobblestone",
    "#DUSTREDSTONE": "#c:dusts/redstone",
    "#DYEBLACK": "#c:dyes/black", "#DYEBLUE": "#c:dyes/blue", "#DYEGRAY": "#c:dyes/gray",
    "#DYEGREEN": "#c:dyes/green", "#DYELIGHTGRAY": "#c:dyes/light_gray", "#DYERED": "#c:dyes/red",
    "#DYEWHITE": "#c:dyes/white", "#DYEYELLOW": "#c:dyes/yellow",
    "#ENDERPEARL": "#c:ender_pearls",
    "#FEATHER": "minecraft:feather",
    "#FENCEWOOD": "#minecraft:wooden_fences",
    "#GEARIRON": f"#{NS}:gears/iron",
    "#GEARSTEEL": f"#{NS}:gears/steel",
    "#GEARWOOD": f"#{NS}:gears/wooden",
    "#INGOTGOLD": "#c:ingots/gold",
    "#INGOTIRON": "#c:ingots/iron",
    "#INGOTSTEEL": f"#{NS}:ingots/steel",
    "#LEATHER": "#c:leathers",
    "#LOGWOOD": "#minecraft:logs",
    "#OBSIDIAN": "minecraft:obsidian",
    "#PANEGLASS": "#c:glass_panes",
    "#PAPER": "minecraft:paper",
    "#SHAFTIRON": f"#{NS}:shafts/iron",
    "#SHAFTSTEEL": f"#{NS}:shafts/steel",
    "#SHAFTWOOD": f"#{NS}:shafts/wooden",
    "#STICKWOOD": "#c:rods/wooden",
    "#STONE": "minecraft:stone",
    "#STRING": "minecraft:string",
    "#WORKBENCH": "minecraft:crafting_table",
    "#GEMDIAMOND": "#c:gems/diamond",
}

# Noms oredict (champ "ore") -> tags
ORE = {
    "ingotIron": "#c:ingots/iron", "ingotGold": "#c:ingots/gold", "ingotSteel": f"#{NS}:ingots/steel",
    "gearWood": f"#{NS}:gears/wooden", "gearIron": f"#{NS}:gears/iron", "gearSteel": f"#{NS}:gears/steel",
    "shaftWood": f"#{NS}:shafts/wooden", "shaftIron": f"#{NS}:shafts/iron", "shaftSteel": f"#{NS}:shafts/steel",
    "string": "minecraft:string", "leather": "#c:leathers", "stickWood": "#c:rods/wooden",
    "plankWood": "#minecraft:planks", "chestWood": "#c:chests/wooden", "cobblestone": "#c:cobblestone",
    "gemDiamond": "#c:gems/diamond", "dustRedstone": "#c:dusts/redstone", "logWood": "#minecraft:logs",
    "feather": "minecraft:feather", "paper": "minecraft:paper", "stone": "minecraft:stone",
    "obsidian": "minecraft:obsidian", "paneGlass": "#c:glass_panes", "enderpearl": "#c:ender_pearls",
    "fenceWood": "#minecraft:wooden_fences", "workbench": "minecraft:crafting_table",
    "gunpowder": "minecraft:gunpowder", "cropWheat": "minecraft:wheat", "dyeBlack": "#c:dyes/black",
    "torch": "minecraft:torch", "sand": "minecraft:sand", "netherrack": "minecraft:netherrack",
    "gemEmerald": "#c:gems/emerald", "dyeBlue": "#c:dyes/blue",
    "bearingWood": f"#{NS}:bearings/wooden", "bearingIron": f"#{NS}:bearings/iron", "bearingSteel": f"#{NS}:bearings/steel",
}

# ancientwarfare:component + métadonnée N (ItemComponent d'origine, ordre des sous-objets)
COMPONENT = ["wooden_gear_set", "iron_gear_set", "steel_gear_set", "wooden_bearings", "iron_bearings",
             "steel_bearings", "wooden_torque_shaft", "iron_torque_shaft", "steel_torque_shaft"]

# Objets vanilla renommés ou éclatés en 1.21 (métadonnée -> objet distinct)
VANILLA_RENAME = {
    "minecraft:noteblock": "minecraft:note_block",
    "minecraft:golden_rail": "minecraft:powered_rail",
    "minecraft:stonebrick": "minecraft:stone_bricks",
}
WOOL = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
DYE = ["black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray",
       "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white"]
FISH = ["cod", "salmon", "tropical_fish", "pufferfish"]


def resolve_vanilla(item, data):
    """Retourne l'identifiant 1.21 pour un objet vanilla 1.12 (nom et métadonnée), ou None."""
    if item in VANILLA_RENAME:
        return VANILLA_RENAME[item]
    if item == "minecraft:wool" and 0 <= data < 16:
        return f"minecraft:{WOOL[data]}_wool"
    if item == "minecraft:dye" and 0 <= data < 16:
        return f"minecraft:{DYE[data]}_dye"
    if item == "minecraft:fish" and 0 <= data < 4:
        return f"minecraft:{FISH[data]}"
    if item == "minecraft:fish":
        return "minecraft:cod"
    return None


# Métadonnées vanilla équivalentes à un autre objet
DATA_FIX = {("minecraft:coal", 1): "minecraft:charcoal"}

# Objets vanilla dont l'identifiant ne change pas (vérifié au cas par cas dans le rapport)
VANILLA_OK_DATA0 = True

# Tags du mod à créer
MOD_TAGS = {
    f"gears/wooden": [f"{NS}:wooden_gear_set"],
    f"gears/iron": [f"{NS}:iron_gear_set"],
    f"gears/steel": [f"{NS}:steel_gear_set"],
    f"shafts/wooden": [f"{NS}:wooden_torque_shaft"],
    f"shafts/iron": [f"{NS}:iron_torque_shaft"],
    f"shafts/steel": [f"{NS}:steel_torque_shaft"],
    f"ingots/steel": [f"{NS}:steel_ingot"],
    f"bearings/wooden": [f"{NS}:wooden_bearings"],
    f"bearings/iron": [f"{NS}:iron_bearings"],
    f"bearings/steel": [f"{NS}:steel_bearings"],
}

report = collections.defaultdict(list)
stats = collections.Counter()


def convert_item(entry, where):
    """Retourne un ingrédient 1.21 ({item}|{tag}) ou None si non convertible."""
    if not isinstance(entry, dict):
        report["skip"].append(f"{where}: ingrédient invalide {entry}")
        return None
    if "ore" in entry:
        name = entry["ore"]
        tag = ORE.get(name)
        if tag is None:
            report["skip"].append(f"{where}: oredict inconnu '{name}'")
            return None
        return ingredient_from_id(tag, where)
    item = entry.get("item")
    if item is None:
        report["skip"].append(f"{where}: pas d'objet {entry}")
        return None
    if item == "ancientwarfare:component":
        idx = entry.get("data", 0)
        if 0 <= idx < len(COMPONENT):
            return ingredient_from_id(f"{NS}:{COMPONENT[idx]}", where)
        report["skip"].append(f"{where}: composant inconnu {idx}")
        return None
    resolved = resolve_vanilla(item, entry.get("data", 0))
    if resolved is not None:
        return ingredient_from_id(resolved, where)
    if (item, entry.get("data", 0)) in DATA_FIX:
        return ingredient_from_id(DATA_FIX[(item, entry.get("data", 0))], where)
    if entry.get("data", 0) not in (0, 32767) and not item.startswith("#"):
        report["skip"].append(f"{where}: métadonnée {entry.get('data')} sur {item} (à mapper à la main)")
        return None
    if "nbt" in entry:
        report["skip"].append(f"{where}: NBT requis sur {item} (non converti)")
        return None
    return ingredient_from_id(item, where)


def ingredient_from_id(ident, where):
    if ident.startswith("#"):
        if ident in TAGS:
            ident = TAGS[ident]
        elif not ident.startswith(("#c:", "#minecraft:", f"#{NS}:")):
            report["skip"].append(f"{where}: tag inconnu {ident}")
            return None
    ident = PORTED.get(ident, ident)
    if ident in AW_ITEMS:
        ident = AW_ITEMS[ident]
    elif ident.startswith("ancientwarfare:") or ident.startswith("ancientwarfarevehicle:") or ident.startswith("ancientwarfareautomation:") or ident.startswith("ancientwarfarenpc:") or ident.startswith("ancientwarfarestructure:"):
        report["skip"].append(f"{where}: objet non porté {ident}")
        return None
    if ident.startswith("#"):
        return {"tag": ident[1:]}
    if ident.startswith("minecraft:") or ident.startswith(f"{NS}:"):
        return {"item": ident}
    report["skip"].append(f"{where}: identifiant inattendu {ident}")
    return None


def convert_result(res, where):
    if not isinstance(res, dict) or "item" not in res:
        report["skip"].append(f"{where}: résultat invalide")
        return None
    ident = res["item"]
    # Arbres de couple : la métadonnée choisit léger / moyen / lourd, qui sont trois blocs distincts
    if ident == "ancientwarfareautomation:torque_shaft":
        tiers = ["light", "medium", "heavy"]
        idx = res.get("data", 0)
        if not (0 <= idx < len(tiers)):
            report["skip"].append(f"{where}: arbre de couple inconnu {idx}")
            return None
        ident = f"{NS}:torque_shaft_{tiers[idx]}"
        res = {"item": ident, "count": res.get("count", 1)}
    # Distributeurs de couple : même principe, léger / moyen / lourd selon la métadonnée
    if ident == "ancientwarfareautomation:torque_junction":
        tiers = ["light", "medium", "heavy"]
        idx = res.get("data", 0)
        if not (0 <= idx < len(tiers)):
            report["skip"].append(f"{where}: jonction inconnue {idx}")
            return None
        ident = f"{NS}:torque_junction_{tiers[idx]}"
        res = {"item": ident, "count": res.get("count", 1)}
    if ident == "ancientwarfareautomation:torque_distributor":
        tiers = ["light", "medium", "heavy"]
        idx = res.get("data", 0)
        if not (0 <= idx < len(tiers)):
            report["skip"].append(f"{where}: distributeur inconnu {idx}")
            return None
        ident = f"{NS}:torque_distributor_{tiers[idx]}"
        res = {"item": ident, "count": res.get("count", 1)}
    if ident == "ancientwarfare:component":
        idx = res.get("data", 0)
        if not (0 <= idx < len(COMPONENT)):
            report["skip"].append(f"{where}: composant résultat inconnu {idx}")
            return None
        ident = f"{NS}:{COMPONENT[idx]}"
        res = {"item": ident, "count": res.get("count", 1)}
    ident = PORTED.get(ident, ident)
    if ident in AW_ITEMS:
        ident = AW_ITEMS[ident]
    elif ident.startswith("minecraft:") and resolve_vanilla(ident, res.get("data", 0)):
        ident = resolve_vanilla(ident, res.get("data", 0))
    elif not (ident.startswith("minecraft:") or ident.startswith(f"{NS}:")):
        report["skip"].append(f"{where}: résultat non porté {ident}")
        return None
    if res.get("data", 0) not in (0, None):
        report["skip"].append(f"{where}: résultat avec métadonnée {res.get('data')} sur {ident}")
        return None
    out = {"id": ident}
    if res.get("count", 1) != 1:
        out["count"] = res["count"]
    return out


def convert_crafting(j, where):
    t = j["type"]
    result = convert_result(j.get("result"), where)
    if result is None:
        return None
    if t in ("forge:ore_shaped", "minecraft:crafting_shaped"):
        pattern = j["pattern"]
        key = {}
        for k, v in j["key"].items():
            ing = convert_item(v if isinstance(v, dict) else {"item": v}, where)
            if ing is None:
                return None
            key[k] = ing
        return {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": key, "result": result}
    if t == "minecraft:crafting_shapeless":
        ings = []
        for v in j["ingredients"]:
            ing = convert_item(v, where)
            if ing is None:
                return None
            ings.append(ing)
        return {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ings, "result": result}
    report["skip"].append(f"{where}: type de recette non géré {t}")
    return None


def convert_research_recipe(j, where):
    """Recette de recherche -> recette de craft standard avec le champ "research" (verrouillée à l'établi)."""
    result = convert_result(j.get("result"), where)
    if result is None:
        return None
    base = {"category": "misc", "research": j["research"], "result": result}
    if j["type"].endswith("shapeless_research_recipe"):
        ings = []
        for v in j.get("ingredients", []):
            ing = convert_item(v, where)
            if ing is None:
                return None
            ings.append(ing)
        base.update({"type": "minecraft:crafting_shapeless", "ingredients": ings})
    else:
        key = {}
        for k, v in j.get("key", {}).items():
            ing = convert_item(v, where)
            if ing is None:
                return None
            key[k] = ing
        base.update({"type": "minecraft:crafting_shaped", "pattern": j["pattern"], "key": key})
    return base


def convert_goal(j, where):
    if j.get("name") is None:
        return None
    resources = []
    for r in j.get("resources", []):
        count = r.get("count", 1)
        if "ore" in r or "item" in r:
            ing = convert_item(r if "item" in r else {"ore": r["ore"]}, where)
        else:
            ing = None
        if ing is None:
            report["skip"].append(f"{where}: ressource de recherche non convertie {r}")
            return None
        resources.extend([ing] * count)  # une entrée par unité consommée
    return {"name": j["name"], "time": j["time"], "dependencies": j.get("dependencies", []), "resources": resources}


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


for f in sorted(glob.glob(os.path.join(SRC, "recipes", "**", "*.json"), recursive=True)):
    name = os.path.basename(f)
    if name.startswith("_"):
        continue
    where = os.path.relpath(f, SRC)
    try:
        j = json.load(open(f, encoding="utf-8"))
    except Exception as e:
        report["skip"].append(f"{where}: JSON invalide {e}")
        continue
    if not isinstance(j, dict) or "type" not in j:
        continue
    out = convert_crafting(j, where)
    if out is not None:
        write(os.path.join(OUT, "recipe", name), out)
        stats["recipe"] += 1

for f in sorted(glob.glob(os.path.join(SRC, "research_recipes", "**", "*.json"), recursive=True)):
    name = os.path.basename(f)
    if name.startswith("_"):
        continue
    where = os.path.relpath(f, SRC)
    try:
        j = json.load(open(f, encoding="utf-8"))
    except Exception as e:
        report["skip"].append(f"{where}: JSON invalide {e}")
        continue
    if not isinstance(j, dict) or "type" not in j:
        continue
    out = convert_research_recipe(j, where)
    if out is not None:
        sub = os.path.relpath(os.path.dirname(f), os.path.join(SRC, "research_recipes"))
        prefix = "" if sub == "." else sub.replace(os.sep, "_") + "_"
        write(os.path.join(OUT, "recipe", "research", prefix + name), out)
        stats["research_recipe"] += 1

for f in sorted(glob.glob(os.path.join(SRC, "registry", "research", "*.json"))):
    where = os.path.relpath(f, SRC)
    try:
        j = json.load(open(f, encoding="utf-8"))
    except Exception as e:
        report["skip"].append(f"{where}: JSON invalide {e}")
        continue
    out = convert_goal(j, where)
    if out is not None:
        write(os.path.join(OUT, "research", os.path.basename(f)), out)
        stats["research"] += 1

for tag, items in MOD_TAGS.items():
    write(os.path.join(OUT, "tags", "item", tag + ".json"), {"values": items})
    stats["tags"] += 1

with open(os.path.join(OUT, "conversion_report.txt"), "w", encoding="utf-8") as f:
    f.write("Résumé : " + ", ".join(f"{k}={v}" for k, v in sorted(stats.items())) + "\n\n")
    f.write(f"Ignorés ({len(report['skip'])}) :\n")
    for line in report["skip"]:
        f.write("  - " + line + "\n")
print("Résumé :", dict(stats), "ignorés :", len(report["skip"]))

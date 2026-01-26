import pandas as pd
import re
import os
import json

# --- 1. CONFIGURAZIONE ---
FILE_INPUT = 'cars.csv'
FILE_OUTPUT = 'cars_final.csv'
FILE_BRAIN = 'ai_brain.json'


# --- 2. FUNZIONI DI SUPPORTO ---
def load_brain():
    if os.path.exists(FILE_BRAIN):
        with open(FILE_BRAIN, 'r') as f:
            return json.load(f)
    return {}


def save_brain(brain):
    with open(FILE_BRAIN, 'w') as f:
        json.dump(brain, f, indent=4)


def clean_base_string(brand, full_model):
    no_year = re.sub(r'\d{4}$', '', str(full_model)).strip()
    no_brand = re.sub(f"^{brand}", "", no_year, flags=re.IGNORECASE).strip()
    return no_brand


# --- 3. LOGICA PRINCIPALE ---
def process_dataset():
    df = pd.read_csv(FILE_INPUT)
    brain = load_brain()

    print(f"Caricate {len(df)} auto. Inizio apprendimento...")
    print("------------------------------------------------")
    print("ISTRUZIONI: Premi [INVIO] per accettare il suggerimento.")
    print("Oppure scrivi il nome corretto della famiglia.")
    print("Scrivi 'EXIT' per salvare e uscire.")
    print("------------------------------------------------")

    new_families = []
    new_trims = []



    for index, row in df.iterrows():
        print(f"Processate {index}/{len(df)} righe, {index / len(df) * 100:.2f}%")
        brand = str(row['brand'])
        raw_model = clean_base_string(brand, row['model'])


        first_word = raw_model.split(' ')[0] if raw_model else "UNKNOWN"
        brain_key = f"{brand.upper()}_{first_word.upper()}"

        family_name = ""

        # A. Controllo se c'è già
        if brain_key in brain:
            family_name = brain[brain_key]

        # B. Richiesta all'utente
        else:
            # Suggerimento automatico: Brand + Prima Parola
            suggestion = f"{brand} {first_word}"

            print(f"\nAuto: {brand} {raw_model}")
            user_input = input(f"Famiglia suggerita: [{suggestion}] > ")

            if user_input.lower() == 'exit':
                break

            if user_input.strip() == "":
                family_name = suggestion  # L'utente ha premuto Invio
            else:
                family_name = user_input.strip()  # L'utente ha corretto

            # Regola imparata
            brain[brain_key] = family_name
            save_brain(brain)

        # C. CALCOLO IL TRIM (VERSIONE)
        # Eliminazione della famiglia dalla stringa completa per ottenere il trim
        # Es: "Fiat Panda 1.2 Lounge" - "Fiat Panda" = "1.2 Lounge"

        # Rimozione del brand dalla famiglia per fare il match pulito (es "Fiat Panda" -> "Panda")
        family_clean = re.sub(f"^{brand}", "", family_name, flags=re.IGNORECASE).strip()

        trim = raw_model.replace(family_clean, "").strip()

        new_families.append(family_name)
        new_trims.append(trim)

        # Barra di caricamento testuale

    # In caso di uscita precoce (EXIT), salviamo parzialmente
    if len(new_families) < len(df):
        print("\nHai interrotto lo script. I dati non sono completi.")

    else:
        def pulisci_colonna(col, un):
            return pd.to_numeric(col.astype(str).str.replace(un, '', regex=False), errors='coerce')
        df['model_family'] = new_families
        df['trim'] = new_trims
        df.drop(["top_speed.1"], axis=1, inplace=True)
        df["turbo"] = df["turbo"].str.replace(",", "")
        df.drop(["ai_label"], axis=1, inplace=True)
        df["acc_0_100"] = df["acc_0_100"].str.replace(",", ".")
        df["acc_0_100"] = pulisci_colonna(df["acc_0_100"], ' s')
        df = df.dropna(subset=["acc_0_100"])
        df["top_speed"] = pulisci_colonna(df["top_speed"], ' km/h')
        df = df.dropna(subset=["top_speed"])
        df["top_speed"] = df["top_speed"].astype(int)
        df["torque"] = pulisci_colonna(df["torque"], ' nm')
        df = df.dropna(subset=["torque"])
        df["torque"] = df["torque"].astype(int)
        df["power"] = pulisci_colonna(df["power"], '')
        df = df.dropna(subset=["power"])
        df["power"] = df["power"].astype(int)
        df["eng_capacity"] = pulisci_colonna(df["eng_capacity"], ' cc')
        df = df.dropna(subset=["eng_capacity"])
        df["eng_capacity"] = df["eng_capacity"].astype(int)
        df["weight"] = pulisci_colonna(df["weight"], ' kg')
        df = df.dropna(subset=["weight"])
        df["weight"] = df["weight"].astype(int)
        df["length"] = pulisci_colonna(df["length"], ' mm')
        df = df.dropna(subset=["length"])
        df["length"] = df["length"].astype(int)
        # Salvataggio finale
        df.to_csv(FILE_OUTPUT, index=False)
        print(f"\nFinito! Dataset salvato in {FILE_OUTPUT}")


if __name__ == "__main__":
    process_dataset()

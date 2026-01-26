import os
import shutil
import pandas as pd
from bing_image_downloader import downloader
from PIL import Image
from tqdm import tqdm

# --- CONFIGURAZIONE ---
CSV_PATH = 'cars_final.csv'
OUTPUT_DIR = 'android_images'
TARGET_WIDTH = 800
TARGET_FORMAT = 'WEBP'
EXTENSION = '.webp'


def clean_name_for_android(name):
    """
    Pulisce il nome per renderlo un nome file valido per Android.
    """
    if pd.isna(name): return "unknown"
    clean = str(name).lower().strip()
    clean = clean.replace(" ", "_").replace("-", "_").replace("'", "")
    clean = "".join([c for c in clean if c.isalnum() or c == "_"])
    return clean


def process_image(src_path, dest_path):
    """
    Ottimizza l'immagine: Ridimensiona -> Converte -> Salva.
    """
    try:
        with Image.open(src_path) as img:
            if img.mode in ("RGBA", "P"):
                img = img.convert("RGB")

            # Calcolo proporzioni
            w_percent = (TARGET_WIDTH / float(img.size[0]))
            h_size = int((float(img.size[1]) * float(w_percent)))

            # Ridimensionamento di alta qualità
            img = img.resize((TARGET_WIDTH, h_size), Image.Resampling.LANCZOS)

            # Salvataggio
            img.save(dest_path, TARGET_FORMAT, quality=80, optimize=True)
            return True
    except Exception as e:
        tqdm.write(f"❌ Errore processando immagine: {e}")
        return False


def main():
    # Creazione cartella output
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    print("Leggendo il CSV...")
    try:
        df = pd.read_csv(CSV_PATH, sep=',')
        # Presi solo i nomi unici per evitare doppi download
        families = df['model_family'].unique()
        families = [f for f in families if not pd.isna(f) and f != ""]
        print(f"Trovati {len(families)} modelli unici da controllare.")
    except Exception as e:
        print(f"Errore lettura CSV: {e}")
        return

    print("Inizio controllo e download...")
    print("-" * 50)

    # --- INIZIO LOOP CON BARRA DI AVANZAMENTO (tqdm) ---
    for family in tqdm(families, desc="Avanzamento", unit="auto"):

        # 1. Calcola il nome del file
        android_name = clean_name_for_android(family)
        final_path = os.path.join(OUTPUT_DIR, android_name + EXTENSION)

        # 2. CONTROLLO INTELLIGENTE: Se il file c'è già, SALTA SUBITO
        if os.path.exists(final_path):
            tqdm.write(f"⏭Saltato (già presente): {family}")
            continue

        # 3. Download immagine
        tqdm.write(f"⬇Download nuovo: {family}...")

        try:
            # Scarica in temp
            downloader.download(
                family + " photo",
                limit=1,
                output_dir='temp_download',
                adult_filter_off=True,
                force_replace=False,
                timeout=5,
                verbose=False
            )

            downloaded_folder = os.path.join('temp_download', family + " photo")

            # 4. Trova e Processa
            if os.path.exists(downloaded_folder):
                files = os.listdir(downloaded_folder)
                if files:
                    raw_path = os.path.join(downloaded_folder, files[0])
                    success = process_image(raw_path, final_path)

                    if success:
                        tqdm.write(f"Salvato: {android_name}")
                    else:
                        tqdm.write(f"Errore salvataggio: {family}")

                # Pulizia immediata cartella specifica
                shutil.rmtree(downloaded_folder, ignore_errors=True)
            else:
                tqdm.write(f"Nessuna immagine trovata su Bing per: {family}")

        except Exception as e:
            tqdm.write(f"Errore download {family}: {e}")

    # Pulizia finale cartella temp root
    if os.path.exists('temp_download'):
        shutil.rmtree('temp_download', ignore_errors=True)

    print("-" * 50)
    print(f"Finito! Immagini aggiornate in: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()

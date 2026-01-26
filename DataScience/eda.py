import os

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# --- CONFIGURAZIONE STILE ---
sns.set_theme(style="whitegrid")
plt.rcParams['figure.figsize'] = (10, 6)
SAVEDIR = 'EDA'


def load_data(filepath):
    """Carica il dataset e stampa info base."""
    df = pd.read_csv(filepath)
    print(f"✅ Dataset caricato: {df.shape[0]} righe, {df.shape[1]} colonne")
    print("\n--- Tipi di Dati ---")
    print(df.dtypes)
    print("\n--- Statistiche Descrittive ---")
    print(df.describe())
    return df


def plot_distributions(df):
    """Analizza la distribuzione delle variabili numeriche chiave."""
    # 1. Anni di Produzione
    plt.figure()
    sns.histplot(df['year'], bins=20, kde=True, color='skyblue')
    plt.title('Distribuzione Anno di Produzione', fontsize=16)
    plt.xlabel('Anno')
    plt.ylabel('Conteggio Auto')
    plt.savefig(os.path.join(SAVEDIR, 'year.png'))
    plt.show()

    # 2. Potenza (CV)
    plt.figure()
    sns.histplot(df['power'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione Potenza (CV)', fontsize=16)
    plt.xlabel('Cavalli (HP/CV)')
    plt.savefig(os.path.join(SAVEDIR, 'power.png'))

    plt.show()

    # 3. Coppia (NM)
    plt.figure()
    sns.histplot(df['torque'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione Coppia (NM)', fontsize=16)
    plt.xlabel('Coppia (NM)')
    plt.savefig(os.path.join(SAVEDIR, 'torque.png'))

    plt.show()

    # 4. Top Speed (KM/H)
    plt.figure()
    sns.histplot(df['top_speed'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione Velocità Max', fontsize=16)
    plt.xlabel('Top Speed (Km/h)')
    plt.savefig(os.path.join(SAVEDIR, 'top_speed.png'))

    plt.show()

    # 5. 0-100 (s)
    plt.figure()
    sns.histplot(df['acc_0_100'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione 0-100', fontsize=16)
    plt.xlabel('Accelerazione 0-100 (s)')
    plt.savefig(os.path.join(SAVEDIR, 'acc.png'))

    plt.show()

    # 6. Cilindrata (cc)
    plt.figure()
    sns.histplot(df['eng_capacity'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione Cilindrata', fontsize=16)
    plt.xlabel('Cilindrata (cc)')
    plt.savefig(os.path.join(SAVEDIR, 'capacity.png'))

    plt.show()

    # 7. Peso (Kg)
    plt.figure()
    sns.histplot(df['weight'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione Peso', fontsize=16)
    plt.xlabel('Peso (Kg)')
    plt.savefig(os.path.join(SAVEDIR, 'peso.png'))

    plt.show()

    # 8. Lunghezza (mm)
    plt.figure()
    sns.histplot(df['length'].dropna(), bins=30, kde=True, color='orange')
    plt.title('Distribuzione Lunghezza', fontsize=16)
    plt.xlabel('Lunghezza (mm)')
    plt.savefig(os.path.join(SAVEDIR, 'lungh.png'))

    plt.show()


def plot_categorical_analysis(df):
    """Analizza le variabili categoriche (Carburante, Cambio, Trazione)."""

    # 1. Carburante
    plt.figure()
    ax = sns.countplot(y="fuel", data=df, order=df['fuel'].value_counts().index, palette="viridis")
    plt.title('Distribuzione Tipo Carburante', fontsize=16)
    plt.xlabel('Numero Auto')
    plt.ylabel('Carburante')
    plt.savefig(os.path.join(SAVEDIR, 'carb.png'))

    plt.show()

    # 2. Brand
    plt.figure()
    ax = sns.countplot(y="brand", data=df, order=df['brand'].value_counts().index, palette="viridis")
    plt.title('Distribuzione Brand', fontsize=16)
    plt.xlabel('Numero Auto')
    plt.ylabel('Brand')
    plt.tight_layout()
    plt.savefig(os.path.join(SAVEDIR, 'brand.png'))

    plt.show()

    # 3. Cylinders
    plt.figure()
    ax = sns.countplot(y="cylinders", data=df, order=df['cylinders'].value_counts().index, palette="viridis")
    plt.title('Distribuzione Cilindri', fontsize=16)
    plt.xlabel('Numero Auto')
    plt.ylabel('Cilindri')
    plt.savefig(os.path.join(SAVEDIR, 'cilindri.png'))

    plt.show()

    # 4. Trasmissione
    plt.figure()
    ax = sns.countplot(y="transmission", data=df, order=df['transmission'].value_counts().index, palette="viridis")
    plt.title('Distribuzione Trasmissione', fontsize=16)
    plt.xlabel('Numero Auto')
    plt.ylabel('Trasmissione')
    plt.tight_layout()
    plt.savefig(os.path.join(SAVEDIR, 'trasm.png'))

    plt.show()

    # 5. Turbo
    plt.figure()
    ax = sns.countplot(y="turbo", data=df, order=df['turbo'].value_counts().index, palette="viridis")
    plt.title('Distribuzione Turbo', fontsize=16)
    plt.xlabel('Numero Auto')
    plt.ylabel('Turbo')
    plt.tight_layout()
    plt.savefig(os.path.join(SAVEDIR, 'turbo.png'))

    plt.show()

    # 6. Trazione
    plt.figure()
    ax = sns.countplot(y="drive_wheel", data=df, order=df['drive_wheel'].value_counts().index, palette="viridis")
    plt.title('Distribuzione Trazione', fontsize=16)
    plt.xlabel('Numero Auto')
    plt.ylabel('Trazione')
    plt.savefig(os.path.join(SAVEDIR, 'trazione.png'))

    plt.show()


# --- ESECUZIONE ---
if __name__ == "__main__":
    CSV_PATH = 'cars_final.csv'

    try:
        df = load_data(CSV_PATH)

        print("\nGenerazione Grafici EDA...")
        plot_distributions(df)
        plot_categorical_analysis(df)

        print("\nAnalisi completata.")

    except FileNotFoundError:
        print(f"Errore: File '{CSV_PATH}' non trovato. Assicurati di aver eseguito cleaner.py prima.")

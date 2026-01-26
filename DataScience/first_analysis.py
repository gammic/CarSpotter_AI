import pandas as pd
import numpy as np
import re

df = pd.read_csv("cars.csv")
auto = [f for f in df["model"]]
marchi = [m for m in df["brand"]]
anni = [a for a in df["year"]]
colonne = ['brand', 'model', 'cylinders', 'transmission', 'power', 'torque',
           'top_speed', 'turbo', 'fuel', 'acc_0_100', 'eng_capacity',
           'drive_wheel', 'weight', 'length']
allowed_brands = ['Abarth', 'Alfa Romeo', 'Alpine', 'Aston Martin', 'Audi',
                  'Austin', 'Autobianchi', 'BMW', 'Bentley', 'Chevrolet', 'Citroen',
                  'Cupra', 'DS', 'Dacia', 'Daihatsu', 'Ferrari', 'Fiat', 'Ford', 'Honda', 'Hummer',
                  'Hyundai', 'Infiniti', 'Jaguar', 'Jeep', 'Kia', 'Lamborghini', 'Lancia', 'Land Rover',
                  'Lexus', 'Lotus', 'Maserati', 'Mazda', 'McLaren', 'Mclaren', 'Mercedes-Benz', 'Mini',
                  'Mitsubishi', 'Nissan', 'Opel', 'Peugeot', 'Polestar', 'Porsche', 'Renault', 'Rolls-Royce',
                  'Seat', 'Skoda' 'Smart', 'Subaru', 'Suzuki', 'TVR', 'Toyota', 'Volkswagen' 'Volvo']
df = df[df["brand"].isin(allowed_brands)]

df.to_csv("cars.csv", index=False)
print(len(df))

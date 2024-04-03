import random
import string

n = int(input("Quantidade de usuários a gerar: "))

usernames = []
passwords = []

for _ in range(n):
    username = ''.join(random.choices(string.ascii_lowercase, k=random.randint(4, 20)))
    password = ''.join(random.choices(string.ascii_letters + string.digits, k=random.randint(1, 20)))
    usernames.append(username)
    passwords.append(password)

with open("./files/input_random.txt", "w") as f:
    for username, password in zip(usernames, passwords):
        f.write(f"{username} {password}\n")
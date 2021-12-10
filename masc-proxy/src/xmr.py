# # Use monero python library as a wrapper for proxy ops
import sys
from monero.seed import Seed

def generate_seed():
    s = Seed()

    # Printing the keys
    print('{} "address": "{}","sk_private": "{}","vk_private": "{}",'
        .format("{",s.public_address(),s.secret_spend_key(),s.secret_view_key()) +
        '"sk_public": "{}", "vk_public": "{}", "phrase": "{}" {}'
        .format(s.public_spend_key(),s.public_view_key(),s.phrase,"}"))
    sys.stdout.flush()

if (sys.argv[1] == "seed"):
    generate_seed()
else:
    print("Invalid argument.")
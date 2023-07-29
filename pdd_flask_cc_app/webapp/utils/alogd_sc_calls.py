import os

import algosdk
from algosdk import account, abi
from algosdk.atomic_transaction_composer import AccountTransactionSigner, AtomicTransactionComposer
from algosdk.v2client import algod


def set_latlon_in_chain(status, address, lat, lon, app_id, dispatcher_mn):
    algod_address = "https://testnet-api.algonode.cloud"
    algod_token = ""
    res = None
    try:
        algod_client = algod.AlgodClient(algod_token, algod_address)

        sk = algosdk.mnemonic.to_private_key(dispatcher_mn)
        addr = account.address_from_private_key(sk)

        # Create signer object
        signer = AccountTransactionSigner(sk)

        # Get suggested params from the client
        sp = algod_client.suggested_params()

        root_directory = os.path.dirname(os.path.abspath(__file__))
        file_path = os.path.join(root_directory, "contract.json")

        with open(
                file_path) as f:
            js = f.read()
        contract = abi.Contract.from_json(js)
        set_latlon_method = contract.get_method_by_name("set_latlon")

        atc = AtomicTransactionComposer()
        atc.add_method_call(
            app_id,
            set_latlon_method,
            addr,
            sp,
            signer,
            method_args=[status, address, lat, lon],
        )
        result = atc.execute(algod_client, 4)
        res = result.abi_results[0].return_value
    except Exception as e:
        print(e)
        res = None
        print(e)
    finally:
        return res

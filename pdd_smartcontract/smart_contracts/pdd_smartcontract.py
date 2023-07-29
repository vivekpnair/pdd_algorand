import beaker as bk
import pyteal as pt
from beaker import Authorize
from pyteal import Bytes, Concat, Global, Assert

# change to test git

class LatLonState:
    tgt_addr = bk.GlobalStateValue(pt.TealType.bytes)
    lat = bk.GlobalStateValue(pt.TealType.bytes)
    status = bk.GlobalStateValue(pt.TealType.bytes)
    lat = bk.GlobalStateValue(pt.TealType.bytes)
    lon = bk.GlobalStateValue(pt.TealType.bytes)


app_base_name = "Latlon_"
app_version = "1"  # change this each time...or better try to get a ddmmyyhhmmss to this

app = bk.Application(app_base_name + app_version, state=LatLonState())


# @app.external(authorize=Authorize.only(Global.creator_address()))

@app.external()
def set_latlon(status: pt.abi.String, tgt_addr: pt.abi.String, lat: pt.abi.String, lon: pt.abi.String, *,
               output: pt.abi.String) -> pt.Expr:
    co_ord_details = Concat(tgt_addr.get(), Bytes(":"), lat.get(), Bytes(":"), lon.get())
    return pt.Seq(
        app.state.tgt_addr.set(tgt_addr.get()),
        app.state.status.set(status.get()),
        app.state.lat.set(lat.get()),
        app.state.lon.set(lon.get()),
        output.set(co_ord_details)
    )


@app.external(read_only=True)
def get_latlon(sender: pt.abi.String, *, output: pt.abi.String) -> pt.Expr:
    sender = sender.get()
    trg_addr = app.state.tgt_addr.get()
    lat = app.state.lat.get()
    lon = app.state.lon.get()
    status = app.state.status.get()
    is_sender_correct = sender == trg_addr
    # {"status":"created","lat":"8.49170234962342","lon":"76.9612322250063"}
    lat_lon = Concat(Bytes("{\"status\":\""), status, Bytes("\""),
                     Bytes(",\"lat\":"), Bytes("\""), lat, Bytes("\""),
                     Bytes(",\"lon\":"), Bytes("\""), lon, Bytes("\"}"))
    return pt.Seq(
        Assert(is_sender_correct),
        output.set(lat_lon)
    )


if __name__ == "__main__":
    spec = app.build()
    spec.export("artifacts")

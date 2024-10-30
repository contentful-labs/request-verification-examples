defmodule RequestVerification do
  use Plug.Router
  require Logger

  plug :match
  plug :dispatch

  @secret System.get_env("CONTENTFUL_SIGNING_SECRET") || "default_secret"

  post "/" do
    {:ok, body, conn} = Plug.Conn.read_body(conn)
    headers = Enum.into(conn.req_headers, %{})

    if verify_request(@secret, conn.method, conn.request_path, headers, body) do
      Logger.info("Request verified successfully")
      send_resp(conn, 200, Jason.encode!(%{message: "Hello, World!"}))
    else
      Logger.warning("Signature verification failed")
      send_resp(conn, 403, Jason.encode!(%{error: "Invalid signature"}))
    end
  end

  def verify_request(secret, method, path, headers, body) do
    signature = Map.get(headers, "x-contentful-signature")
    signed_headers = Map.get(headers, "x-contentful-signed-headers", "") |> String.split(",")

    canonical_string = build_canonical_string(method, path, headers, signed_headers, body)
    generated_signature = calculate_signature(secret, canonical_string)

    Plug.Crypto.secure_compare(generated_signature, signature)
  end

  defp build_canonical_string(method, path, headers, signed_headers, body) do
    headers_string =
      Enum.map(signed_headers, fn header ->
        header_key = String.downcase(header)
        value = Map.get(headers, header_key, "")
        "#{header_key}:#{value}"
      end)
      |> Enum.join(";")

    canonical_string = "#{method}\n#{path}\n#{headers_string}\n#{body}"
    canonical_string
  end

  defp calculate_signature(secret, canonical_string) do
    :crypto.mac(:hmac, :sha256, secret, canonical_string)
    |> Base.encode16(case: :lower)
  end
end

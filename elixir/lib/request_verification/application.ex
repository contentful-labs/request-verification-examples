defmodule RequestVerification.Application do
  use Application

  def start(_type, _args) do
    children = [
      {Plug.Cowboy, scheme: :http, plug: RequestVerification, options: [port: 8080]}
    ]

    opts = [strategy: :one_for_one, name: RequestVerification.Supervisor]
    Supervisor.start_link(children, opts)
  end
end

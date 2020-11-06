package workshop.infra

final case class Config(db: DbConfig, api: ApiConfig)

final case class DbConfig(url: String, user: String, pass: String, driver: String)

final case class ApiConfig(host: String, port: Int)

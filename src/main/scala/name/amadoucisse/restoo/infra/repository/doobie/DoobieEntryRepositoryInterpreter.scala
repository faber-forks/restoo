package name.amadoucisse.restoo
package infra
package repository.doobie

import cats._
import cats.implicits._

import doobie._
import doobie.implicits._

import domain.items.ItemId
import domain.entries.{Entry, EntryId, EntryRepositoryAlgebra}

private object EntrySQL extends SQLCommon {

  def insert(entry: Entry): Update0 = sql"""
    INSERT INTO entries (item_id, delta)
    VALUES (${entry.itemId}, ${entry.delta})
  """.update

  def count(itemId: ItemId): Query0[Option[Long]] = sql"""
    SELECT
      SUM(delta) as quantity
    FROM entries
    WHERE item_id = $itemId
  """.query[Option[Long]]

}

final class DoobieEntryRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends EntryRepositoryAlgebra[F] {

  def create(entry: Entry): F[Entry] = {
    val program = for {
      res <- EntrySQL
        .insert(entry)
        .withUniqueGeneratedKeys[Int]("id")
        .map(id => entry.copy(id = EntryId(id).some))

      _ <- ItemSQL.touch(entry.itemId).run
    } yield res

    program.transact(xa)
  }

  def count(id: ItemId): F[Option[Long]] = EntrySQL.count(id).unique.transact(xa)
}

object DoobieEntryRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieEntryRepositoryInterpreter[F] =
    new DoobieEntryRepositoryInterpreter(xa)
}

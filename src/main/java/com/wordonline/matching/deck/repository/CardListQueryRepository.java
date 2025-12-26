package com.wordonline.matching.deck.repository;

import com.wordonline.matching.deck.domain.Card;
import com.wordonline.matching.deck.dto.MyCardListRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;

public interface CardListQueryRepository extends R2dbcRepository<Card, Long> {

    @Query("""
select
  c.id as "id",
  c.name as "name",
  c.card_type::text as "type",
  coalesce(uc.count, 0) as "count",
  (uc.card_id is not null) as "unlocked",
  case
    when uc.card_id is null and c.unlock_condition_type = 'WIN_COUNT'
      then (c.unlock_required_value::text || '승')
    else null
  end as "unlockText",
  case
    when uc.card_id is null and c.unlock_condition_type = 'WIN_COUNT'
      then (least(u.total_wins, c.unlock_required_value)::text || '/' || c.unlock_required_value::text)
    else null
  end as "progressText"
from cards c
join users u on u.id = :userId
left join user_cards uc
  on uc.user_id = :userId and uc.card_id = c.id
order by c.id
""")
    Flux<MyCardListRow> findMyCardList(long userId);
}
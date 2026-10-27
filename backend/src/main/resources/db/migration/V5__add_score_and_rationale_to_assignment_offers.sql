ALTER TABLE assignment_offers
    ADD COLUMN match_score NUMERIC(5, 2),
    ADD COLUMN match_rationale TEXT;

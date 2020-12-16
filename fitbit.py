from sqlalchemy import (
    Column,
    DateTime,
    Integer,
    Float,
    String,
    Text,
    ForeignKey
)
from sqlalchemy.ext.declarative import declarative_base
from dictalchemy import DictableModel

Base = declarative_base(cls=DictableModel)

class FitbitDailyActivity(Base):
    __tablename__ = "fitbit_daily_activity"

    id = Column(String, primary_key=True)
    participantId = Column("participant_id", Integer, ForeignKey("participant.participant_id"), nullable=False)
    date = Column("date", DateTime)
    activityCalories = Column("activity_calories", Integer)
    caloriesBMR = Column("calories_bmr", Integer)
    caloriesOut = Column("calories_out", Integer)
    marginalCalories = Column("marginal_calories", Integer)
    steps = Column("steps", Integer)
    distance = Column("distance", Float) # going to have to be a sum of the distances...
    elevation = Column("elevation", Float)
    floors = Column("floors", Integer)
    veryActiveMinutes = Column("very_active_minutes", Integer)
    fairlyActiveMinutes = Column("fairly_active_minutes", Integer)
    lightlyActiveMinutes = Column("lightly_active_minutes", Integer)
    sedentaryMinutes = Column("sedentary_minutes", Integer)

class FitbitHRSummary(Base):
    __tablename__ = "fitbit_hr_summary"

    id = Column(String, primary_key=True)
    participantId = Column("participant_id", Integer, ForeignKey("participant.participant_id"), nullable=False)
    datetime = Column("datetime", DateTime, nullable=False)
    zone = Column("zone", Text, nullable=False)
    min = Column("min", Integer, nullable=True)
    max = Column("max", Integer, nullable=True)
    minutes = Column("minutes", Integer, nullable=False)

class FitbitIntradayHR(Base):
    __tablename__ = "fitbit_intraday_hr"

    id = Column(String, primary_key=True)
    participantId = Column("participant_id", Integer, ForeignKey("participant.participant_id"), nullable=False)
    datetime = Column("datetime", DateTime, nullable=False)
    value = Column("value", Integer, nullable=False)

class FitbitIntradaySteps(Base):
    __tablename__ = "fitbit_intraday_steps"

    id = Column(String, primary_key=True)
    participantId = Column("participant_id", Integer, ForeignKey("participant.participant_id"), nullable=False)
    datetime = Column("datetime", DateTime, nullable=False)
    value = Column("value", Integer, nullable=False)

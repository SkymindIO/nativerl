from typing import Dict

import ray
from ray.rllib.env import BaseEnv
from ray.rllib.policy import Policy
from ray.rllib.evaluation import MultiAgentEpisode, RolloutWorker
from ray.rllib.agents.callbacks import DefaultCallbacks


def get_callbacks(debug_metrics, is_gym):

    class Callbacks(DefaultCallbacks):
        def on_episode_start(self, worker: RolloutWorker, base_env: BaseEnv,
                             policies: Dict[str, Policy],
                             episode: MultiAgentEpisode, **kwargs):
            episode.hist_data["metrics_raw"] = []

        def on_episode_end(self, worker: RolloutWorker, base_env: BaseEnv,
                           policies: Dict[str, Policy], episode: MultiAgentEpisode, **kwargs):
            if not is_gym:
                metrics = worker.env.getMetrics().tolist()

                if debug_metrics:
                    episode.hist_data["metrics_raw"] = metrics

                for i, val in enumerate(metrics):
                    episode.custom_metrics["metrics_" + str(i)] = metrics[i]

        def on_train_result(self, trainer, result: dict, **kwargs):
            if not is_gym:
                results = ray.get(
                    [w.apply.remote(lambda worker: worker.env.getMetrics()) for w in trainer.workers.remote_workers()])

                result["last_metrics"] = results[0].tolist() if results is not None and len(results) > 0 else -1

    return Callbacks
#!/usr/bin/env python3
"""
Telegram notification script for GitHub Actions.
Sends formatted notifications for pull requests, issues, and push events to Telegram.
"""

import os
import sys
import json
import urllib.request
import urllib.parse
from html import escape


def get_env_var(name, required=True):
    """Get environment variable with optional requirement check."""
    value = os.getenv(name)
    if required and not value:
        print(f"Error: Required environment variable {name} is not set", file=sys.stderr)
        sys.exit(1)
    return value


def get_pr_status(action, merged):
    """Convert GitHub PR action to human-readable status."""
    if action == "closed" and merged:
        return "merged"
    elif action == "synchronize":
        return "updated"
    else:
        return action


def format_pr_message(repo, actor, pr_num, pr_title, pr_url, base_ref, head_ref, status):
    """Format the Telegram message for pull requests."""
    # Escape all text content for HTML
    repo_esc = escape(repo)
    actor_esc = escape(actor)
    pr_num_esc = escape(str(pr_num))
    pr_title_esc = escape(pr_title)
    base_ref_esc = escape(base_ref)
    head_ref_esc = escape(head_ref)
    status_esc = escape(status)

    # Build message (URL doesn't need escaping in href attribute)
    message = f"""<b>PR {status_esc}:</b> <a href="{pr_url}">#{pr_num_esc}</a> — {pr_title_esc}
Repo: {repo_esc}
By: {actor_esc}
{head_ref_esc} → {base_ref_esc}"""

    return message


def format_issue_message(repo, actor, issue_num, issue_title, issue_url, status):
    """Format the Telegram message for issues."""
    # Escape all text content for HTML
    repo_esc = escape(repo)
    actor_esc = escape(actor)
    issue_num_esc = escape(str(issue_num))
    issue_title_esc = escape(issue_title)
    status_esc = escape(status)

    # Build message
    message = f"""<b>Issue {status_esc}:</b> <a href="{issue_url}">#{issue_num_esc}</a> — {issue_title_esc}
Repo: {repo_esc}
By: {actor_esc}"""

    return message


def format_push_message(repo, actor, branch, commits_data):
    """Format the Telegram message for push events."""
    # Escape all text content for HTML
    repo_esc = escape(repo)
    actor_esc = escape(actor)
    branch_esc = escape(branch)

    # Parse commits JSON if provided
    commits_list = []
    if commits_data:
        try:
            commits = json.loads(commits_data)
            # Take first 10 commits
            for commit in commits[:10]:
                sha7 = commit.get('id', '')[:7]
                full_sha = commit.get('id', '')
                message = commit.get('message', '').replace('\n', ' ')
                author = commit.get('author', {}).get('username') or commit.get('author', {}).get('name') or 'unknown'

                msg_esc = escape(message)
                author_esc = escape(author)
                url = f"https://github.com/{repo}/commit/{full_sha}"

                commits_list.append(f"- <a href=\"{url}\">{sha7}</a> — {msg_esc} (by {author_esc})")
        except (json.JSONDecodeError, KeyError, TypeError):
            commits_list = ["— error parsing commits —"]

    if not commits_list:
        commits_list = ["— no commit messages —"]

    commits_text = '\n'.join(commits_list)

    # Build message
    message = f"""{actor_esc} just pushed to branch {branch_esc} of {repo_esc}:
{commits_text}"""

    return message


def format_workflow_message(repo, actor, workflow_name, conclusion, event_type, branch, run_url):
    """Format the Telegram message for workflow runs."""
    # Escape all text content for HTML
    repo_esc = escape(repo)
    actor_esc = escape(actor)
    workflow_name_esc = escape(workflow_name)
    conclusion_esc = escape(conclusion)
    event_type_esc = escape(event_type)
    branch_esc = escape(branch)

    # Build message
    message = f"""<b>Workflow:</b> {workflow_name_esc}
Repo: {repo_esc}
Branch: {branch_esc}
By: {actor_esc}
Event: {event_type_esc}
Status: <b>{conclusion_esc}</b>
<a href="{run_url}">Open run</a>"""

    return message


def send_telegram_message(bot_token, chat_id, message, thread_id=None):
    """Send message to Telegram using Bot API."""
    url = f"https://api.telegram.org/bot{bot_token}/sendMessage"

    data = {
        "chat_id": chat_id,
        "text": message,
        "parse_mode": "HTML"
    }

    # Add thread_id if provided
    if thread_id:
        try:
            # Try to convert to int, fall back to string if needed
            data["message_thread_id"] = int(thread_id)
        except (ValueError, TypeError):
            data["message_thread_id"] = thread_id

    # Encode data
    data_encoded = urllib.parse.urlencode(data).encode('utf-8')

    # Create request
    req = urllib.request.Request(url, data=data_encoded, method='POST')
    req.add_header('Content-Type', 'application/x-www-form-urlencoded')

    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode('utf-8'))
            if result.get('ok'):
                print("✅ Telegram message sent successfully")
                return True
            else:
                print(f"❌ Telegram API error: {result.get('description', 'Unknown error')}", file=sys.stderr)
                return False
    except Exception as e:
        print(f"❌ Failed to send Telegram message: {e}", file=sys.stderr)
        return False


def main():
    """Main function to process GitHub event and send Telegram notification."""
    # Get required environment variables
    bot_token = get_env_var('TELEGRAM_BOT_TOKEN')
    chat_id = get_env_var('TELEGRAM_CHAT_ID')

    # Get optional thread ID
    thread_id = get_env_var('TELEGRAM_THREAD_ID', required=False)

    # Get common GitHub event data
    repo = get_env_var('GITHUB_REPOSITORY')
    actor = get_env_var('GITHUB_ACTOR')

    # Determine event type and format message accordingly
    event_type = get_env_var('EVENT_TYPE', required=False)

    if event_type == 'pull_request' or get_env_var('PR_NUMBER', required=False):
        # Pull Request event
        pr_num = get_env_var('PR_NUMBER')
        pr_title = get_env_var('PR_TITLE')
        pr_url = get_env_var('PR_URL')
        base_ref = get_env_var('BASE_REF')
        head_ref = get_env_var('HEAD_REF')
        action = get_env_var('PR_ACTION')
        merged = get_env_var('PR_MERGED', required=False) == 'true'

        status = get_pr_status(action, merged)
        message = format_pr_message(repo, actor, pr_num, pr_title, pr_url, base_ref, head_ref, status)

    elif event_type == 'issues' or get_env_var('ISSUE_NUMBER', required=False):
        # Issue event
        issue_num = get_env_var('ISSUE_NUMBER')
        issue_title = get_env_var('ISSUE_TITLE')
        issue_url = get_env_var('ISSUE_URL')
        status = get_env_var('ISSUE_ACTION')

        message = format_issue_message(repo, actor, issue_num, issue_title, issue_url, status)

    elif event_type == 'push' or get_env_var('PUSH_BRANCH', required=False):
        # Push event
        branch = get_env_var('PUSH_BRANCH')
        commits_data = get_env_var('PUSH_COMMITS', required=False)

        message = format_push_message(repo, actor, branch, commits_data)

    elif event_type == 'workflow_run' or get_env_var('WORKFLOW_NAME', required=False):
        # Workflow run event
        workflow_name = get_env_var('WORKFLOW_NAME')
        conclusion = get_env_var('WORKFLOW_CONCLUSION')
        workflow_event = get_env_var('WORKFLOW_EVENT', required=False) or 'unknown'
        branch = get_env_var('WORKFLOW_BRANCH', required=False) or 'unknown'
        run_url = get_env_var('WORKFLOW_URL', required=False) or '#'

        message = format_workflow_message(repo, actor, workflow_name, conclusion, workflow_event, branch, run_url)

    else:
        print(f"❌ Unknown event type or missing required environment variables", file=sys.stderr)
        sys.exit(1)

    # Send to Telegram
    success = send_telegram_message(bot_token, chat_id, message, thread_id)

    if not success:
        sys.exit(1)


if __name__ == "__main__":
    main()
